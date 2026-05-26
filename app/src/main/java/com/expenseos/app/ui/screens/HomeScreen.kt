package com.expenseos.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import com.expenseos.app.core.model.TransactionStatus
import com.expenseos.app.features.home.HomeTab
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.Coral
import com.expenseos.app.ui.theme.CoralPink
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Ink
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.Paper
import com.expenseos.app.ui.theme.Pine
import com.expenseos.app.ui.theme.Seafoam
import com.expenseos.app.features.insights.InsightCardModel
import com.expenseos.app.features.insights.InsightType
import androidx.compose.foundation.lazy.LazyRow
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material.icons.filled.Add
import com.expenseos.app.features.scanner.ReceiptScannerScreen
import androidx.compose.material3.FloatingActionButton

@Composable
fun HomeScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    var isSimulatorExpanded by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        ReceiptScannerScreen(
            onScanSuccess = { candidate ->
                showScanner = false
                // Use existing simulate pipeline to handle adding to DB and Inbox
                // We'll adapt it to add directly using a new viewmodel function or just a hack via text
                viewModel.simulateParsedText(candidate.rawText, false) // Fallback via text, but ideally we add a direct method.
            },
            onClose = { showScanner = false }
        )
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Header(isPremium = uiState.isPremiumUser)
                }
                item {
                    GamificationStrip(
                        streak = uiState.currentStreak,
                        challenge = uiState.dailyChallenge
                    )
                }
                item {
                    HealthCard(
                        score = uiState.health.score,
                        label = uiState.health.label,
                        explanation = uiState.health.explanation
                    )
                }
                if (uiState.insights.isNotEmpty()) {
                    item {
                        InsightsRow(insights = uiState.insights)
                    }
                }
                if (uiState.budgets.isNotEmpty()) {
                    item {
                        BudgetStrip(budgets = uiState.budgets)
                    }
                }
                item {
                    TabRow(
                        selected = uiState.selectedHomeTab,
                        onSelected = viewModel::selectHomeTab,
                        inboxCount = uiState.candidates.count { it.status == TransactionStatus.SUGGESTED }
                    )
                }

                val verifiedCandidates = uiState.candidates.filter { it.status == TransactionStatus.CONFIRMED }
                val suggestedCandidates = uiState.candidates.filter { it.status == TransactionStatus.SUGGESTED }

                when (uiState.selectedHomeTab) {
                    HomeTab.Timeline -> {
                        item { TimelineSummary(verifiedCandidates) }
                        if (verifiedCandidates.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "Timeline is empty",
                                    description = "Verify raw SMS transactions in the Inbox or use the simulation console below to log new transactions."
                                )
                            }
                        } else {
                            items(verifiedCandidates, key = { it.id }) { candidate ->
                                TransactionRow(candidate = candidate)
                            }
                        }
                    }
                    HomeTab.Inbox -> {
                        if (suggestedCandidates.isNotEmpty()) {
                            item {
                                InboxPrompt(
                                    suggestedCount = suggestedCandidates.size,
                                    onConfirmAll = viewModel::confirmAllSuggested
                                )
                            }
                            items(suggestedCandidates, key = { it.id }) { candidate ->
                                TransactionRow(
                                    candidate = candidate,
                                    showConfidence = true,
                                    onConfirm = { viewModel.confirmCandidate(candidate.id) }
                                )
                            }
                        } else {
                            item {
                                EmptyStateCard(
                                    title = "All clear!",
                                    description = "No pending suggestions. Any new financial SMS or voice entry will populate here for review."
                                )
                            }
                        }
                    }
                    HomeTab.Rewind -> {
                        item { RewindCard(verifiedCandidates) }
                    }
                }
            }

            // Floating Action Button for Scanner
            FloatingActionButton(
                onClick = { showScanner = true },
                containerColor = Mint,
                contentColor = Pine,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp) // Offset above simulator
            ) {
                Icon(Icons.Default.Add, contentDescription = "Scan Receipt")
            }

            // Collapsible Simulator HUD at the absolute bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SimulatorConsole(
                    isExpanded = isSimulatorExpanded,
                    onToggle = { isSimulatorExpanded = !isSimulatorExpanded },
                    onSimulate = viewModel::simulateParsedText
                )
            }
        }
    }
}

@Composable
private fun Header(isPremium: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Expense OS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Ink
            )
            Text(
                text = "Your money, automatically understood.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
        }
        if (isPremium) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(Pine, Coral)))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PRO MEMBER",
                    color = Gold,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HealthCard(score: Int, label: String, explanation: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Pine, Color(0xFF00C78C))))
                .fillMaxWidth()
        ) {
            Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("FINANCIAL HEALTH", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(label, color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Text(explanation, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
    }
}

@Composable
private fun TabRow(selected: HomeTab, onSelected: (HomeTab) -> Unit, inboxCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = selected == tab
            val labelText = if (tab == HomeTab.Inbox && inboxCount > 0) {
                "${tab.name} ($inboxCount)"
            } else {
                tab.name
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelected(tab) },
                label = { 
                    Text(
                        labelText, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Pine,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = MutedInk
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Pine.copy(alpha = 0.5f), 
                    borderWidth = 2.dp
                ),
                shape = CircleShape
            )
        }
    }
}

@Composable
private fun TimelineSummary(candidates: List<TransactionCandidate>) {
    val spend = candidates.filter { it.direction == TransactionDirection.EXPENSE }.sumOf { it.amount }
    val income = candidates.filter { it.direction == TransactionDirection.INCOME }.sumOf { it.amount }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard("Spent this month", formatPkr(spend), Coral, Modifier.weight(1f))
        MetricCard("Income recorded", formatPkr(income), Pine, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, color = MutedInk, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(value, color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TransactionRow(
    candidate: TransactionCandidate,
    showConfidence: Boolean = false,
    onConfirm: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 1.0f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SourceDot(candidate.source)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${candidate.category.label} · ${candidate.occurredAt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
                if (showConfidence) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = "Confidence", 
                            tint = if (candidate.confidence > 0.8f) Pine else Gold,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "AI Confidence ${(candidate.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (candidate.confidence > 0.8f) MutedInk else Coral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = signedAmount(candidate),
                    color = if (candidate.direction == TransactionDirection.INCOME) Pine else Coral,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                if (onConfirm != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.5f))
                            .clickable { onConfirm() }
                    ) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = "Confirm", 
                            tint = Pine,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceDot(source: TransactionSource) {
    val (color, text) = when (source) {
        TransactionSource.SMS -> Pair(Gold, "SMS")
        TransactionSource.VOICE -> Pair(Coral, "VCE")
        TransactionSource.RECEIPT -> Pair(Mint, "RCP")
        else -> Pair(Pine, "MAN")
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            color = if (color == Mint) Pine else color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun InboxPrompt(suggestedCount: Int, onConfirmAll: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Seafoam),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Pending Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Pine)
            Text(
                "Verify these $suggestedCount suggested transactions to immediately earn Expense Coins and secure your balance book accuracy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
            Button(
                onClick = onConfirmAll,
                colors = ButtonDefaults.buttonColors(containerColor = Pine),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Confirm All suggestions", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RewindCard(candidates: List<TransactionCandidate>) {
    val expenses = candidates.filter { it.direction == TransactionDirection.EXPENSE }
    val topCategory = expenses
        .groupBy { it.category.label }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
        ?.key ?: "No Expenses Yet"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Seafoam),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("May Rewind Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Pine)
            Text(
                "Your highest category this month is $topCategory. Automated tracking shows most expenses occurred in the late afternoon. Watch out for spontaneous eating out and cash drafts.",
                style = MaterialTheme.typography.bodyLarge,
                color = MutedInk
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Paper)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Recommendation: Check your Inbox daily and log cash Udhaars immediately.",
                    color = Pine,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Pine)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SimulatorConsole(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onSimulate: (String, Boolean) -> Unit
) {
    val smsPresets = listOf(
        "Easypaisa: You sent Rs. 3,500 to K-Electric. Txn ID 99281.",
        "HBL Alert: PKR 1,800 debited at Metro Mart on 26-May.",
        "JazzCash: You received Rs. 20,000 from Farhan Ahmed."
    )

    val voicePresets = listOf(
        "Spent 1500 on dinner at Al-Rehman Biryani",
        "Groceries pe 3000 kharch kiya"
    )

    Card(
        shape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Text(
                        "Local Intelligence Playground",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Pine
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = "Toggle simulator"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Select a preset banking SMS or voice command to simulate local detection on the handset.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )

                    Text("SMS SIMULATIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Pine)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        smsPresets.forEach { preset ->
                            PresetRow(preset = preset, onClick = { onSimulate(preset, false) })
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("VOICE SIMULATIONS (ENG/URDU)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Pine)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        voicePresets.forEach { preset ->
                            PresetRow(preset = preset, onClick = { onSimulate(preset, true) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetRow(preset: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Paper)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = preset,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = "Simulate",
            tint = Pine,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun signedAmount(candidate: TransactionCandidate): String {
    val prefix = when (candidate.direction) {
        TransactionDirection.INCOME -> "+"
        TransactionDirection.REFUND -> "+"
        else -> "-"
    }
    return "$prefix${formatPkr(candidate.amount)}"
}

private fun formatPkr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
    return "Rs ${formatter.format(amount)}"
}

@Composable
private fun InsightsRow(insights: List<InsightCardModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "FINANCIAL INSIGHTS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MutedInk,
            modifier = Modifier.padding(start = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(insights, key = { it.id }) { insight ->
                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (insight.type) {
                            InsightType.WARNING -> CoralPink.copy(alpha = 0.1f)
                            InsightType.CELEBRATION -> Seafoam
                            else -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (insight.type) {
                                InsightType.WARNING -> Coral
                                InsightType.CELEBRATION -> Pine
                                else -> Ink
                            }
                        )
                        Text(
                            text = insight.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetStrip(budgets: List<com.expenseos.app.core.model.BudgetEnvelope>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "BUDGET HEALTH",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MutedInk,
            modifier = Modifier.padding(start = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(budgets, key = { it.id }) { budget ->
                val progressColor = when {
                    budget.progress >= 1f -> CoralPink
                    budget.progress > 0.8f -> Gold
                    else -> Pine
                }
                Card(
                    modifier = Modifier.width(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = budget.category.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                progress = { budget.progress },
                                modifier = Modifier.size(60.dp),
                                color = progressColor,
                                trackColor = progressColor.copy(alpha = 0.2f),
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "${(budget.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Ink
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamificationStrip(streak: Int, challenge: com.expenseos.app.features.gamification.DailyChallenge?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Streak Card
        Card(
            modifier = Modifier.weight(0.4f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (streak > 0) CoralPink.copy(alpha = 0.1f) else Color.White),
            border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (streak > 0) "🔥" else "🧊",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "$streak Days",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = if (streak > 0) CoralPink else MutedInk
                )
                Text(
                    text = "Streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
            }
        }

        // Daily Challenge Card
        if (challenge != null) {
            Card(
                modifier = Modifier.weight(0.6f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (challenge.isCompleted) Seafoam else Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = challenge.emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Column {
                            Text(
                                text = "Daily Challenge",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (challenge.isCompleted) Pine else MutedInk
                            )
                            Text(
                                text = challenge.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (challenge.isCompleted) Pine else Ink
                            )
                        }
                    }
                    if (challenge.isCompleted) {
                        Text(
                            text = "Completed! +${challenge.coinReward} Coins",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Pine,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
