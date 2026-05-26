package com.expenseos.app.features.parsing

import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import java.time.ZonedDateTime

class VoiceTransactionParser(
    private val categoryClassifier: CategoryClassifier = CategoryClassifier()
) : TransactionParser {
    private val amountRegex = Regex("""([0-9,]+(?:\.\d{1,2})?)""")

    override fun parse(rawText: String): TransactionCandidate? {
        val amount = amountRegex.find(rawText)
            ?.value
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?.toLong()
            ?: return null

        val direction = inferDirection(rawText)
        val category = categoryClassifier.classify(rawText)

        return TransactionCandidate(
            amount = amount,
            direction = direction,
            merchant = inferMerchant(rawText, category.label),
            category = category,
            source = TransactionSource.VOICE,
            occurredAt = ZonedDateTime.now(),
            confidence = if (direction == TransactionDirection.UNKNOWN) 0.62f else 0.82f,
            rawText = rawText
        )
    }

    private fun inferDirection(text: String): TransactionDirection {
        val normalized = text.lowercase()
        return when {
            listOf("received", "mila", "salary", "income").any(normalized::contains) -> TransactionDirection.INCOME
            listOf("spent", "paid", "kharch", "diya", "bheja").any(normalized::contains) -> TransactionDirection.EXPENSE
            else -> TransactionDirection.UNKNOWN
        }
    }

    private fun inferMerchant(text: String, fallback: String): String {
        val normalized = text.replace(Regex("""\s+"""), " ").trim()
        return Regex("""(?i)(?:on|for|pe|from|to) ([A-Za-z0-9 ._-]+)$""")
            .find(normalized)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?: fallback
    }
}

