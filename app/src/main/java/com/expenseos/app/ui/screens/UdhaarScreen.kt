package com.expenseos.app.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expenseos.app.core.model.UdhaarEntry
import com.expenseos.app.core.model.UdhaarType
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.UdhaarFilter
import com.expenseos.app.ui.theme.Coral
import com.expenseos.app.ui.theme.CoralPink
import com.expenseos.app.ui.theme.Gold
import com.expenseos.app.ui.theme.Mint
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.Paper
import com.expenseos.app.ui.theme.Pine
import androidx.compose.ui.platform.LocalContext
import com.expenseos.app.features.udhaar.UdhaarReminderHelper
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun UdhaarScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Udhaar Ledger",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Pine
                        )
                        Text(
                            text = "Keep track of informal P2P loans cleanly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                }

                // Balance Sheet cards
                item {
                    BalanceSheet(entries = uiState.udhaarEntries)
                }

                // Filter tabs (Given vs Received)
                item {
                    UdhaarFilterRow(
                        selected = uiState.selectedUdhaarTab,
                        onSelected = viewModel::selectUdhaarTab
                    )
                }

                // List filter logic
                val currentType = if (uiState.selectedUdhaarTab == UdhaarFilter.GIVEN) UdhaarType.GIVEN else UdhaarType.RECEIVED
                val activeEntries = uiState.udhaarEntries.filter { it.type == currentType && !it.isSettled }
                val settledEntries = uiState.udhaarEntries.filter { it.type == currentType && it.isSettled }

                if (activeEntries.isEmpty() && settledEntries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No records here. Tap the + button to log peer udhaar.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MutedInk,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Active debts
                    if (activeEntries.isNotEmpty()) {
                        item {
                            Text(
                                text = "ACTIVE DEBTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Pine
                            )
                        }
                        items(activeEntries, key = { it.id }) { entry ->
                            UdhaarCard(
                                entry = entry,
                                onSettle = { viewModel.settleUdhaarEntry(entry.id) }
                            )
                        }
                    }

                    // Settled debts
                    if (settledEntries.isNotEmpty()) {
                        item {
                            Text(
                                text = "SETTLED HISTORY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MutedInk,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(settledEntries, key = { it.id }) { entry ->
                            UdhaarCard(entry = entry, onSettle = null)
                        }
                    }
                }
            }

            // Floating Action Button to Add Udhaar
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Pine,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Udhaar")
            }

            // Dialog trigger
            if (showAddDialog) {
                AddUdhaarDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, amount, type, note ->
                        viewModel.addUdhaarEntry(name, amount, type, note)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BalanceSheet(entries: List<UdhaarEntry>) {
    val givenTotal = entries.filter { it.type == UdhaarType.GIVEN && !it.isSettled }.sumOf { it.amount }
    val receivedTotal = entries.filter { it.type == UdhaarType.RECEIVED && !it.isSettled }.sumOf { it.amount }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Owed to you (Given)", color = Pine, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(formatPkr(givenTotal), color = Pine, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CoralPink.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("You owe (Received)", color = Coral, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(formatPkr(receivedTotal), color = Coral, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun UdhaarFilterRow(selected: UdhaarFilter, onSelected: (UdhaarFilter) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (tab in UdhaarFilter.entries) {
            val isSelected = selected == tab
            val tabLabel = if (tab == UdhaarFilter.GIVEN) "Given (I Lent)" else "Received (I Borrowed)"

            FilterChip(
                selected = isSelected,
                onClick = { onSelected(tab) },
                label = { Text(tabLabel, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (tab == UdhaarFilter.GIVEN) Pine else Coral,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = MutedInk
                ),
                border = null,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun UdhaarCard(
    entry: UdhaarEntry,
    onSettle: (() -> Unit)?
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isSettled) Color.White.copy(alpha = 0.6f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (entry.isSettled) 0.dp else 1.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact Circle Avatar
                ContactAvatar(name = entry.contactName, isSettled = entry.isSettled, type = entry.type)

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isSettled) MutedInk else Pine,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!entry.note.isNullOrBlank()) {
                        Text(
                            text = entry.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (entry.isSettled) {
                            "Settled on ${entry.settledAt?.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
                        } else {
                            "Due by ${entry.dueDate?.format(DateTimeFormatter.ofPattern("MMM d"))}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatPkr(entry.amount),
                        color = if (entry.isSettled) MutedInk else if (entry.type == UdhaarType.GIVEN) Pine else Coral,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (onSettle != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Mint.copy(alpha = 0.5f))
                                .clickable { onSettle() }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Settle Debt",
                                tint = Pine,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Action row for active entries
            if (!entry.isSettled && onSettle != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Pine.copy(alpha = 0.05f))
                        .clickable { UdhaarReminderHelper.sendWhatsAppReminder(context, entry) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Send Reminder 📱",
                        color = Pine,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactAvatar(name: String, isSettled: Boolean, type: UdhaarType) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    val color = if (isSettled) {
        MutedInk.copy(alpha = 0.2f)
    } else if (type == UdhaarType.GIVEN) {
        Mint.copy(alpha = 0.6f)
    } else {
        Coral.copy(alpha = 0.2f)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
    ) {
        Text(
            text = initials,
            color = if (isSettled) MutedInk else if (type == UdhaarType.GIVEN) Pine else Coral,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddUdhaarDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, UdhaarType, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(UdhaarType.GIVEN) }
    var note by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Record Udhaar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Pine
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pine,
                        focusedLabelColor = Pine
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pine,
                        focusedLabelColor = Pine
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Given vs Received Toggle Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = UdhaarType.GIVEN },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == UdhaarType.GIVEN) Pine else Paper,
                            contentColor = if (type == UdhaarType.GIVEN) Color.White else MutedInk
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("I Lent (Given)", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { type = UdhaarType.RECEIVED },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == UdhaarType.RECEIVED) Coral else Paper,
                            contentColor = if (type == UdhaarType.RECEIVED) Color.White else MutedInk
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("I Borrowed", fontWeight = FontWeight.Bold)
                    }
                }

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Short note (e.g. lunch, petrol)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pine,
                        focusedLabelColor = Pine
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Text(
                        text = "Please input valid Name and positive Amount number.",
                        color = Coral,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toLongOrNull()
                    if (name.isNotBlank() && amount != null && amount > 0) {
                        onConfirm(name, amount, type, note.takeIf { it.isNotBlank() })
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Pine)
            ) {
                Text("Save Ledger", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Pine)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

private fun formatPkr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK"))
    return "Rs ${formatter.format(amount)}"
}
