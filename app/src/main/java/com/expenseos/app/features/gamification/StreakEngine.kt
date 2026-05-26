package com.expenseos.app.features.gamification

import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionStatus
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object StreakEngine {

    /**
     * Calculates the current streak of consecutive days where the user has at least one CONFIRMED transaction.
     */
    fun calculateCurrentStreak(transactions: List<TransactionCandidate>): Int {
        val confirmedTx = transactions
            .filter { it.status == TransactionStatus.CONFIRMED }
            .sortedByDescending { it.occurredAt }

        if (confirmedTx.isEmpty()) return 0

        // Extract unique days
        val activeDays = confirmedTx.map {
            it.occurredAt.truncatedTo(ChronoUnit.DAYS)
        }.distinct()

        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val yesterday = today.minusDays(1)

        // If the user hasn't logged anything today or yesterday, streak is broken
        if (!activeDays.contains(today) && !activeDays.contains(yesterday)) {
            return 0
        }

        var streak = 0
        var currentDateToCheck = if (activeDays.contains(today)) today else yesterday

        while (activeDays.contains(currentDateToCheck)) {
            streak++
            currentDateToCheck = currentDateToCheck.minusDays(1)
        }

        return streak
    }
}
