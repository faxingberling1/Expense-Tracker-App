package com.expenseos.app.features.gamification

import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionStatus
import java.time.ZonedDateTime
import kotlin.random.Random

data class DailyChallenge(
    val id: String,
    val title: String,
    val emoji: String,
    val coinReward: Int,
    val isCompleted: Boolean
)

object DailyChallengeEngine {

    fun getDailyChallenge(transactions: List<TransactionCandidate>): DailyChallenge {
        val today = ZonedDateTime.now()
        val seed = today.year * 10000 + today.monthValue * 100 + today.dayOfMonth
        val random = Random(seed)

        val challenges = listOf(
            Triple("Log 3 expenses", "📝", 15),
            Triple("Clear your Inbox", "📥", 20),
            Triple("Log a food expense", "🍔", 10),
            Triple("Confirm all transactions", "✅", 25)
        )

        val selectedChallenge = challenges[random.nextInt(challenges.size)]
        
        val isCompleted = evaluateChallenge(selectedChallenge.first, transactions, today)

        return DailyChallenge(
            id = "challenge_$seed",
            title = selectedChallenge.first,
            emoji = selectedChallenge.second,
            coinReward = selectedChallenge.third,
            isCompleted = isCompleted
        )
    }

    private fun evaluateChallenge(title: String, transactions: List<TransactionCandidate>, today: ZonedDateTime): Boolean {
        val todayTransactions = transactions.filter {
            it.occurredAt.year == today.year && 
            it.occurredAt.dayOfYear == today.dayOfYear
        }

        return when (title) {
            "Log 3 expenses" -> todayTransactions.count { it.status == TransactionStatus.CONFIRMED } >= 3
            "Clear your Inbox" -> transactions.none { it.status == TransactionStatus.SUGGESTED }
            "Log a food expense" -> todayTransactions.any { it.category.name == "FOOD" && it.status == TransactionStatus.CONFIRMED }
            "Confirm all transactions" -> transactions.isNotEmpty() && transactions.none { it.status == TransactionStatus.SUGGESTED }
            else -> false
        }
    }
}
