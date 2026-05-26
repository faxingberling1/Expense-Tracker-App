package com.expenseos.app.features.insights

import com.expenseos.app.core.model.Category
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionStatus
import java.util.UUID

class InsightsEngine {

    fun generateInsights(transactions: List<TransactionCandidate>): List<InsightCardModel> {
        val insights = mutableListOf<InsightCardModel>()
        
        val confirmedExpenses = transactions.filter { 
            it.status == TransactionStatus.CONFIRMED && it.direction == TransactionDirection.EXPENSE 
        }

        if (confirmedExpenses.isEmpty()) {
            insights.add(
                InsightCardModel(
                    id = UUID.randomUUID().toString(),
                    title = "Welcome to Expense OS",
                    body = "Log your first expense or let us automatically detect it from your SMS.",
                    type = InsightType.INFO
                )
            )
            return insights
        }

        // 1. Food Alert
        val foodTotal = confirmedExpenses.filter { it.category == Category.FOOD }.sumOf { it.amount }
        val allTotal = confirmedExpenses.sumOf { it.amount }
        
        if (allTotal > 0 && (foodTotal.toFloat() / allTotal) > 0.4f) {
            insights.add(
                InsightCardModel(
                    id = UUID.randomUUID().toString(),
                    title = "Heavy on Food! 🍔",
                    body = "You've spent PKR $foodTotal on food recently, which is over 40% of your expenses.",
                    type = InsightType.WARNING
                )
            )
        }

        // 2. High Value Spending Tip
        val largeExpenses = confirmedExpenses.filter { it.amount > 10000 }
        if (largeExpenses.isNotEmpty()) {
            insights.add(
                InsightCardModel(
                    id = UUID.randomUUID().toString(),
                    title = "Big Purchases 💸",
                    body = "You have ${largeExpenses.size} expenses over PKR 10,000. Try the 48-hour rule next time to curb impulse buys.",
                    type = InsightType.TIP
                )
            )
        }

        // 3. Activity Celebration
        if (confirmedExpenses.size >= 5) {
            insights.add(
                InsightCardModel(
                    id = UUID.randomUUID().toString(),
                    title = "Great Tracking! 🎉",
                    body = "You've successfully logged ${confirmedExpenses.size} expenses. Consistency is key to financial health.",
                    type = InsightType.CELEBRATION
                )
            )
        }

        return insights
    }
}
