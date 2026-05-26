package com.expenseos.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionStatus
import com.expenseos.app.ui.ExpenseOsUiState
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.theme.CoralPink
import com.expenseos.app.ui.theme.Ink
import com.expenseos.app.ui.theme.MutedInk
import com.expenseos.app.ui.theme.OffWhite
import com.expenseos.app.ui.theme.Pine
import com.expenseos.app.ui.theme.SadaTeal
import com.expenseos.app.ui.theme.Seafoam
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class CalendarViewMode { DAY, WEEK, MONTH }

@Composable
fun CalendarScreen(
    uiState: ExpenseOsUiState,
    viewModel: ExpenseOsViewModel
) {
    val confirmedTransactions = uiState.candidates.filter { it.status == TransactionStatus.CONFIRMED }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }

    // Map date → list of transactions for quick lookup
    val transactionsByDate = remember(confirmedTransactions) {
        confirmedTransactions.groupBy { it.occurredAt.toLocalDate() }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = OffWhite) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ─── Header ────────────────────────────────────────────────────
            item {
                CalendarHeader(displayMonth = displayMonth, viewMode = viewMode)
            }

            // ─── View Mode Toggle ──────────────────────────────────────────
            item {
                ViewModeToggle(
                    current = viewMode,
                    onSelect = { viewMode = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ─── Calendar Grid / Week Strip ────────────────────────────────
            item {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "CalendarViewTransition"
                ) { mode ->
                    when (mode) {
                        CalendarViewMode.MONTH -> MonthGrid(
                            month = displayMonth,
                            selectedDate = selectedDate,
                            transactionsByDate = transactionsByDate,
                            onDateClick = { selectedDate = it },
                            onMonthBack = { displayMonth = displayMonth.minusMonths(1) },
                            onMonthForward = { displayMonth = displayMonth.plusMonths(1) }
                        )
                        CalendarViewMode.WEEK -> WeekStrip(
                            selectedDate = selectedDate,
                            transactionsByDate = transactionsByDate,
                            onDateClick = { selectedDate = it }
                        )
                        CalendarViewMode.DAY -> DayView(
                            selectedDate = selectedDate,
                            transactions = transactionsByDate[selectedDate] ?: emptyList(),
                            onBack = { selectedDate = selectedDate.minusDays(1) },
                            onForward = { selectedDate = selectedDate.plusDays(1) }
                        )
                    }
                }
            }

            // ─── Summary Bar ───────────────────────────────────────────────
            item {
                DaySummaryBar(
                    date = selectedDate,
                    transactions = transactionsByDate[selectedDate] ?: emptyList()
                )
            }

            // ─── Transaction List for Selected Date ────────────────────────
            val selected = transactionsByDate[selectedDate] ?: emptyList()
            if (selected.isEmpty()) {
                item {
                    EmptyDayState(date = selectedDate)
                }
            } else {
                item {
                    Text(
                        text = "TRANSACTIONS · ${selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MutedInk,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                items(selected, key = { it.id }) { tx ->
                    CalendarTransactionRow(tx)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CalendarHeader(displayMonth: YearMonth, viewMode: CalendarViewMode) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Pine, SadaTeal))
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Expense Calendar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// View Mode Toggle
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ViewModeToggle(
    current: CalendarViewMode,
    onSelect: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CalendarViewMode.entries.forEach { mode ->
            FilterChip(
                selected = current == mode,
                onClick = { onSelect(mode) },
                label = {
                    Text(
                        mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontWeight = if (current == mode) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Pine,
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

// ─────────────────────────────────────────────────────────────────────────────
// Month Grid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    transactionsByDate: Map<LocalDate, List<TransactionCandidate>>,
    onDateClick: (LocalDate) -> Unit,
    onMonthBack: () -> Unit,
    onMonthForward: () -> Unit
) {
    val today = LocalDate.now()
    val firstDay = month.atDay(1)
    val lastDay = month.atEndOfMonth()
    // Start grid from Monday
    val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Month navigation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMonthBack) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous month", tint = Pine)
            }
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            IconButton(onClick = onMonthForward) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next month", tint = Pine)
            }
        }

        // Day-of-week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar cells
        val totalCells = startOffset + lastDay.dayOfMonth
        val rows = (totalCells + 6) / 7

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - startOffset + 1
                    val date = if (dayNum in 1..lastDay.dayOfMonth) month.atDay(dayNum) else null
                    val hasTx = date != null && transactionsByDate.containsKey(date)
                    val isSelected = date == selectedDate
                    val isToday = date == today

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> Pine
                                    isToday -> Seafoam
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (date != null) Modifier.clickable { onDateClick(date) }
                                else Modifier
                            )
                    ) {
                        if (date != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> Pine
                                        else -> Ink
                                    }
                                )
                                if (hasTx) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else SadaTeal)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Week Strip
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WeekStrip(
    selectedDate: LocalDate,
    transactionsByDate: Map<LocalDate, List<TransactionCandidate>>,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    // Show 3 weeks: prev, current, next (centred on selectedDate's week)
    val weekStart = selectedDate.with(DayOfWeek.MONDAY)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        (-1..1).forEach { weekOffset ->
            val wStart = weekStart.plusWeeks(weekOffset.toLong())
            Column {
                if (weekOffset != -1) HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                Text(
                    text = if (weekOffset == 0) "This Week" else if (weekOffset == -1) "Last Week" else "Next Week",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    (0..6).forEach { d ->
                        val date = wStart.plusDays(d.toLong())
                        val hasTx = transactionsByDate.containsKey(date)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val total = transactionsByDate[date]
                            ?.filter { it.direction == TransactionDirection.EXPENSE }
                            ?.sumOf { it.amount } ?: 0L

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> Pine
                                        isToday -> Seafoam
                                        else -> Color.White
                                    }
                                )
                                .clickable { onDateClick(date) }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MutedInk,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else Ink
                            )
                            if (hasTx) {
                                Box(
                                    modifier = Modifier.size(4.dp).clip(CircleShape)
                                        .background(if (isSelected) Color.White else SadaTeal)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                        if (d < 6) Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                if (hasTxInWeek(wStart, transactionsByDate)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WeekTotalsBar(wStart, transactionsByDate)
                }
            }
        }
    }
}

private fun hasTxInWeek(weekStart: LocalDate, map: Map<LocalDate, List<TransactionCandidate>>): Boolean =
    (0..6).any { map.containsKey(weekStart.plusDays(it.toLong())) }

@Composable
private fun WeekTotalsBar(weekStart: LocalDate, transactionsByDate: Map<LocalDate, List<TransactionCandidate>>) {
    val weekExpenses = (0..6).sumOf { d ->
        transactionsByDate[weekStart.plusDays(d.toLong())]
            ?.filter { it.direction == TransactionDirection.EXPENSE }
            ?.sumOf { it.amount } ?: 0L
    }
    val weekIncome = (0..6).sumOf { d ->
        transactionsByDate[weekStart.plusDays(d.toLong())]
            ?.filter { it.direction == TransactionDirection.INCOME }
            ?.sumOf { it.amount } ?: 0L
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OffWhite)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Week Spend", style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
            Text(formatPkrCal(weekExpenses), style = MaterialTheme.typography.titleSmall, color = CoralPink, fontWeight = FontWeight.Black)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Week Income", style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
            Text(formatPkrCal(weekIncome), style = MaterialTheme.typography.titleSmall, color = Pine, fontWeight = FontWeight.Black)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Day View
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DayView(
    selectedDate: LocalDate,
    transactions: List<TransactionCandidate>,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    val totalSpend = transactions.filter { it.direction == TransactionDirection.EXPENSE }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.direction == TransactionDirection.INCOME }.sumOf { it.amount }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Day navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous day", tint = Pine)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )
            }
            IconButton(onClick = onForward) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next day", tint = Pine)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spend/Income pills
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CoralPink.copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Spent", style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
                    Text(formatPkrCal(totalSpend), style = MaterialTheme.typography.titleMedium, color = CoralPink, fontWeight = FontWeight.Black)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Seafoam)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Received", style = MaterialTheme.typography.labelSmall, color = MutedInk, fontWeight = FontWeight.Bold)
                    Text(formatPkrCal(totalIncome), style = MaterialTheme.typography.titleMedium, color = Pine, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Day Summary Bar (always visible below calendar)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DaySummaryBar(date: LocalDate, transactions: List<TransactionCandidate>) {
    val spend = transactions.filter { it.direction == TransactionDirection.EXPENSE }.sumOf { it.amount }
    val income = transactions.filter { it.direction == TransactionDirection.INCOME }.sumOf { it.amount }
    val count = transactions.size

    if (transactions.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryPill("$count txns", MutedInk, Ink)
            SummaryPill(formatPkrCal(spend), CoralPink.copy(alpha = 0.2f), CoralPink)
            SummaryPill(formatPkrCal(income), Seafoam, Pine)
        }
    }
}

@Composable
private fun SummaryPill(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Calendar Transaction Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CalendarTransactionRow(tx: TransactionCandidate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category dot
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (tx.direction == TransactionDirection.INCOME) Seafoam else CoralPink.copy(alpha = 0.12f))
        ) {
            Text(
                text = tx.category.label.take(2).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (tx.direction == TransactionDirection.INCOME) Pine else CoralPink,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.merchant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${tx.category.label} · ${tx.occurredAt.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk
            )
        }

        Text(
            text = if (tx.direction == TransactionDirection.INCOME) "+${formatPkrCal(tx.amount)}" else "-${formatPkrCal(tx.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = if (tx.direction == TransactionDirection.INCOME) Pine else CoralPink
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyDayState(date: LocalDate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("📅", fontSize = 48.sp, textAlign = TextAlign.Center)
        Text(
            text = "No expenses on\n${date.format(DateTimeFormatter.ofPattern("d MMMM"))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Log a cash expense or let SMS auto-detection do it for you.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatPkrCal(amount: Long): String {
    val fmt = NumberFormat.getNumberInstance(Locale("en", "PK"))
    return "Rs ${fmt.format(amount)}"
}

private fun ZonedDateTime.toLocalDate(): LocalDate = toLocalDate()
