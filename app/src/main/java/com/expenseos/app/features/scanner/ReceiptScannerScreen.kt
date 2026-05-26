package com.expenseos.app.features.scanner

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.expenseos.app.core.model.Category
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import com.expenseos.app.core.model.TransactionStatus
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.Executors

@Composable
fun ReceiptScannerScreen(
    onScanSuccess: (TransactionCandidate) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, ReceiptAnalyzer { amount, merchant ->
                                // Successfully extracted!
                                val candidate = TransactionCandidate(
                                    id = UUID.randomUUID().toString(),
                                    amount = amount,
                                    direction = TransactionDirection.EXPENSE,
                                    merchant = merchant,
                                    category = Category.OTHER, // Default, can be refined
                                    source = TransactionSource.RECEIPT,
                                    occurredAt = ZonedDateTime.now(),
                                    confidence = 0.9f,
                                    rawText = "Receipt OCR: $merchant, PKR $amount",
                                    status = TransactionStatus.SUGGESTED
                                )
                                onScanSuccess(candidate)
                            })
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        Log.e("ReceiptScanner", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close Button
        FloatingActionButton(
            onClick = onClose,
            containerColor = Color.White.copy(alpha = 0.3f),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close Scanner")
        }
    }
}

private class ReceiptAnalyzer(private val onResult: (Long, String) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var hasFired = false

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (hasFired) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    
                    // Basic heuristic: Extract largest number as total amount
                    val amountRegex = Regex("(?i)(total|amount|pkr|rs|amount due)[:\\s]*([\\d,]+\\.?\\d*)")
                    val numbers = Regex("\\d+").findAll(text).mapNotNull { it.value.toLongOrNull() }.toList()
                    
                    if (numbers.isNotEmpty() && !hasFired) {
                        val maxAmount = numbers.maxOrNull() ?: 0L
                        if (maxAmount > 50) { // arbitrary threshold to ignore single digits
                            hasFired = true
                            
                            // Naive merchant extraction: first line of text
                            val firstLine = text.lines().firstOrNull { it.isNotBlank() && !it.contains(Regex("\\d")) } ?: "Unknown Merchant"
                            
                            onResult(maxAmount, firstLine)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ReceiptAnalyzer", "OCR failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
