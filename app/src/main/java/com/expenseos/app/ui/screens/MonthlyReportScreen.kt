package com.expenseos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expenseos.app.core.model.Category
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionStatus
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.*
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthlyReportScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    val previousMonth = YearMonth.now().minusMonths(1)
    
    val transactions = uiState.candidates.filter {
        it.status == TransactionStatus.CONFIRMED && YearMonth.from(it.occurredAt) == previousMonth
    }
    
    val totalSpent = transactions.filter { it.direction == TransactionDirection.EXPENSE }.sumOf { it.amount }
    val totalReceived = transactions.filter { it.direction == TransactionDirection.INCOME }.sumOf { it.amount }
    val netSavings = totalReceived - totalSpent
    
    val topCategories = transactions
        .filter { it.direction == TransactionDirection.EXPENSE }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }
        .take(3)

    Surface(modifier = Modifier.fillMaxSize(), color = Pine) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your ${previousMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Seafoam
                    )
                    Text(
                        text = "Report Card",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SadaTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Net Savings", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatPkr(netSavings),
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("In", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(formatPkr(totalReceived), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Out", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(formatPkr(totalSpent), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            if (topCategories.isNotEmpty()) {
                item {
                    Text(
                        text = "Top Spending",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                topCategories.forEachIndexed { index, entry ->
                    item {
                        CategoryBar(
                            category = entry.key,
                            amount = entry.value,
                            maxAmount = topCategories.first().value,
                            rank = index + 1
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { /* Share intent */ },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Seafoam, contentColor = Pine),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("Share to WhatsApp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(category: Category, amount: Long, maxAmount: Long, rank: Int) {
    val fraction = if (maxAmount > 0) amount.toFloat() / maxAmount.toFloat() else 0f
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Seafoam
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(category.label, color = Color.White, fontWeight = FontWeight.Bold)
                Text(formatPkr(amount), color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CoralPink)
            )
        }
    }
}

private fun formatPkr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
    return "Rs ${formatter.format(amount)}"
}
