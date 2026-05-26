package com.expenseos.app.features.home

import com.expenseos.app.core.model.FinancialHealth
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.features.parsing.SmsTransactionParser
import com.expenseos.app.features.parsing.VoiceTransactionParser

class HomeRepository {
    private val smsParser = SmsTransactionParser()
    private val voiceParser = VoiceTransactionParser()

    fun loadCandidates(): List<TransactionCandidate> {
        val samples = listOfNotNull(
            smsParser.parse("Easypaisa: You have sent Rs. 1,200 to Shell Fuel. Txn ID 88221."),
            smsParser.parse("HBL Alert: PKR 2,450 debited at Foodpanda on 26-May. Available balance updated."),
            smsParser.parse("JazzCash: You received Rs. 15,000 from Ali Khan."),
            voiceParser.parse("Spent 850 on lunch"),
            voiceParser.parse("Kal 2500 groceries pe kharch kiye")
        )
        return samples.sortedByDescending { it.occurredAt }
    }

    fun financialHealth(candidates: List<TransactionCandidate>): FinancialHealth {
        val expenseTotal = candidates
            .filter { it.direction == TransactionDirection.EXPENSE }
            .sumOf { it.amount }
        val score = when {
            expenseTotal < 10_000 -> 82
            expenseTotal < 25_000 -> 71
            else -> 58
        }

        return FinancialHealth(
            score = score,
            label = if (score >= 75) "Steady" else "Watchful",
            explanation = "Food and cash entries are visible early. Confirm suggested items to sharpen your score."
        )
    }
}
