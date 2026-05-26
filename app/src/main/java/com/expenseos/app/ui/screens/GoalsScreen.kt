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
import com.expenseos.app.core.model.SavingsGoal
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoalsScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Savings Goals",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "Save for what matters most",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                }

                if (uiState.goals.isEmpty()) {
                    item {
                        EmptyGoalsState()
                    }
                } else {
                    items(uiState.goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onAddFunds = { amount -> viewModel.addFundsToGoal(goal.id, amount) }
                        )
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
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }

            if (showAddDialog) {
                AddGoalDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title, target, emoji ->
                        viewModel.addSavingsGoal(title, target, emoji)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GoalCard(goal: SavingsGoal, onAddFunds: (Long) -> Unit) {
    val progressColor = if (goal.isCompleted || goal.progress >= 1f) Pine else SadaTeal

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Pine.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Seafoam)
                    ) {
                        Text(
                            text = goal.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = if (goal.isCompleted) "Goal Achieved! 🎉" else "${formatPkr(goal.remainingAmount)} more to go",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (goal.isCompleted) Pine else MutedInk,
                            fontWeight = if (goal.isCompleted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Text(
                    text = "${(goal.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = progressColor
                )
            }

            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                    Text(
                        text = formatPkr(goal.savedAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                    Text(
                        text = formatPkr(goal.targetAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
            }

            if (!goal.isCompleted) {
                Button(
                    onClick = { onAddFunds(500L) }, // Hardcoded for prototype
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Seafoam, contentColor = Pine),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Rs 500", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsState() {
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
            Text("🎯", style = MaterialTheme.typography.headlineLarge)
        }
        Text(
            text = "No Goals Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Pine
        )
        Text(
            text = "Start saving for an emergency fund, a new phone, or a dream vacation.",
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
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎯") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "New Savings Goal",
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Vacation)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Amount (Rs.)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toLongOrNull()
                    if (title.isNotBlank() && target != null && target > 0) {
                        onConfirm(title, target, emoji)
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
