package com.expenseos.app.features.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.expenseos.app.core.model.Category
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import com.expenseos.app.core.model.TransactionStatus
import java.time.ZonedDateTime
import java.util.UUID

@Composable
fun ReceiptScannerScreen(
    onScanSuccess: (TransactionCandidate) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B)) // Deep Slate
    ) {
        // Mock Camera Viewfinder
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(2.dp)
            ) {
                // Viewfinder borders
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Align receipt here",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val candidate = TransactionCandidate(
                        id = UUID.randomUUID().toString(),
                        amount = 4500L,
                        direction = TransactionDirection.EXPENSE,
                        merchant = "Monarch Supermarket",
                        category = Category.FOOD,
                        source = TransactionSource.RECEIPT,
                        occurredAt = ZonedDateTime.now(),
                        confidence = 0.95f,
                        rawText = "Receipt OCR: Monarch Supermarket, PKR 4500",
                        status = TransactionStatus.SUGGESTED
                    )
                    onScanSuccess(candidate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Mint Green
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).padding(horizontal = 32.dp).fillMaxWidth()
            ) {
                Text("Simulate Successful Scan", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Camera access is mocked for the emulator.",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }

        // Close Button
        FloatingActionButton(
            onClick = onClose,
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close Scanner")
        }
    }
}
