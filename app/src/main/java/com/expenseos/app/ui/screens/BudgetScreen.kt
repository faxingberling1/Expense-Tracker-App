package com.expenseos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expenseos.app.core.model.BudgetEnvelope
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.*
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BudgetScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    val currentMonth = YearMonth.now()
    var showAddDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Budgets",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "Control your spending for ${currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                }

                if (uiState.budgets.isEmpty()) {
                    item {
                        EmptyBudgetState()
                    }
                } else {
                    items(uiState.budgets, key = { it.id }) { budget ->
                        BudgetCard(budget = budget)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Pine,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }

            if (showAddDialog) {
                AddBudgetDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { category, limit ->
                        viewModel.setBudgetLimit(category, limit)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetCard(budget: BudgetEnvelope) {
    val progressColor = when {
        budget.progress >= 1f -> CoralPink
        budget.progress > 0.8f -> Gold
        else -> Pine
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Seafoam)
                    ) {
                        Text(
                            text = budget.category.label.take(2).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Pine
                        )
                    }
                    Column {
                        Text(
                            text = budget.category.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "${formatPkr(budget.remainingAmount)} left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                }

                Text(
                    text = "${(budget.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = progressColor
                )
            }

            LinearProgressIndicator(
                progress = { budget.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${formatPkr(budget.spentAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
                Text(
                    text = "Limit: ${formatPkr(budget.limitAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun EmptyBudgetState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Seafoam)
        ) {
            Text("🗂️", style = MaterialTheme.typography.headlineLarge)
        }
        Text(
            text = "No Budgets Set",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Pine
        )
        Text(
            text = "Set limits for your frequent categories like Food or Transport to keep your spending in check.",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun formatPkr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
    return "Rs ${formatter.format(amount)}"
}

@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.expenseos.app.core.model.Category, Long) -> Unit
) {
    var limitStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(com.expenseos.app.core.model.Category.FOOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Pine
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Monthly Limit (Rs.)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", fontWeight = FontWeight.Bold, color = Pine)
                // Simplified category selector for demo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(com.expenseos.app.core.model.Category.FOOD, com.expenseos.app.core.model.Category.TRANSPORT)
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitStr.toLongOrNull()
                    if (limit != null && limit > 0) {
                        onConfirm(selectedCategory, limit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Pine)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Pine)
            }
        },
        containerColor = Color.White
    )
}
