package com.expenseos.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import com.expenseos.app.features.home.HomeTab
import com.expenseos.app.features.home.HomeViewModel
import com.expenseos.app.ui.theme.Coral
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.Paper
import com.expenseos.app.ui.theme.Pine
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExpenseOsApp(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Header()
            }
            item {
                HealthCard(
                    score = uiState.health.score,
                    label = uiState.health.label,
                    explanation = uiState.health.explanation
                )
            }
            item {
                TabRow(
                    selected = uiState.selectedTab,
                    onSelected = viewModel::selectTab
                )
            }
            when (uiState.selectedTab) {
                HomeTab.Timeline -> {
                    item { TimelineSummary(uiState.candidates) }
                    items(uiState.candidates) { candidate ->
                        TransactionRow(candidate = candidate)
                    }
                }
                HomeTab.Inbox -> {
                    item { InboxPrompt() }
                    items(uiState.candidates) { candidate ->
                        TransactionRow(candidate = candidate, showConfidence = true)
                    }
                }
                HomeTab.Rewind -> {
                    item { RewindCard(uiState.candidates) }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Expense OS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Pine
        )
        Text(
            text = "Your money, automatically understood.",
            style = MaterialTheme.typography.bodyLarge,
            color = MutedInk
        )
    }
}

@Composable
private fun HealthCard(score: Int, label: String, explanation: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Pine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Financial Health", color = Mint, style = MaterialTheme.typography.labelLarge)
                    Text(label, color = Gold, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = score.toString(),
                    color = Gold,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Gold,
                trackColor = Mint.copy(alpha = 0.24f)
            )
            Text(explanation, color = Mint, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TabRow(selected: HomeTab, onSelected: (HomeTab) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeTab.entries.forEach { tab ->
            FilterChip(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                label = { Text(tab.name) }
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
        MetricCard("Spent", formatPkr(spend), Coral, Modifier.weight(1f))
        MetricCard("Income", formatPkr(income), Pine, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, accent: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MutedInk, style = MaterialTheme.typography.labelMedium)
            Text(value, color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransactionRow(candidate: TransactionCandidate, showConfidence: Boolean = false) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${candidate.category.label} · ${candidate.source.name.lowercase()} · ${candidate.occurredAt.format(DateTimeFormatter.ofPattern("MMM d"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
                if (showConfidence) {
                    Text(
                        text = "Confidence ${(candidate.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }
            Text(
                text = signedAmount(candidate),
                color = if (candidate.direction == TransactionDirection.INCOME) Pine else Coral,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SourceDot(source: TransactionSource) {
    val color = when (source) {
        TransactionSource.SMS -> Gold
        TransactionSource.VOICE -> Coral
        TransactionSource.RECEIPT -> Mint
        else -> Pine
    }
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun InboxPrompt() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Suggested transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Confirm these to improve the timeline and health score.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Pine)
            ) {
                Text("Confirm all")
            }
        }
    }
}

@Composable
private fun RewindCard(candidates: List<TransactionCandidate>) {
    val topCategory = candidates
        .filter { it.direction == TransactionDirection.EXPENSE }
        .groupBy { it.category.label }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
        ?.key ?: "Food"

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("May Rewind", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Pine)
            Text(
                "Your clearest pattern so far is $topCategory. Late-day food and cash entries should be watched before they become invisible spending.",
                style = MaterialTheme.typography.bodyLarge,
                color = MutedInk
            )
            Text("Best next move: confirm inbox suggestions daily.", color = Pine, fontWeight = FontWeight.SemiBold)
        }
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
    return "PKR ${formatter.format(amount)}"
}

