package com.expenseos.app.features.parsing

import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import java.time.ZonedDateTime

class SmsTransactionParser(
    private val categoryClassifier: CategoryClassifier = CategoryClassifier()
) : TransactionParser {
    // Enhanced to cover "Rs500", "PKR 5,000", "Amount: Rs.50", "Amount PKR 1000"
    private val amountRegex = Regex("""(?i)(?:rs\.?|pkr|amount:?)\s*(?:rs\.?)?\s*([0-9,]+(?:\.\d{1,2})?)|([0-9,]+(?:\.\d{1,2})?)\s*(?:rs\.?|pkr)""")

    override fun parse(rawText: String): TransactionCandidate? {
        // Try both match groups depending on whether currency was prefix or suffix
        val amountStr = amountRegex.find(rawText)?.let { match ->
            match.groupValues[1].takeIf { it.isNotBlank() } ?: match.groupValues[2].takeIf { it.isNotBlank() }
        }

        val amount = amountStr?.replace(",", "")
            ?.toDoubleOrNull()
            ?.toLong()
            ?: return null

        val direction = inferDirection(rawText)
        val merchant = inferMerchant(rawText, direction)
        val category = categoryClassifier.classify(rawText)

        return TransactionCandidate(
            amount = amount,
            direction = direction,
            merchant = merchant,
            category = category,
            source = TransactionSource.SMS,
            occurredAt = ZonedDateTime.now(),
            confidence = confidenceFor(rawText, direction),
            rawText = rawText
        )
    }

    private fun inferDirection(text: String): TransactionDirection {
        val normalized = text.lowercase()
        return when {
            // Expanded keywords for local SMS contexts
            listOf("debited", "purchase", "paid", "sent", "withdrawn", "cash out", "payment of", "deducted").any(normalized::contains) -> TransactionDirection.EXPENSE
            listOf("credited", "received", "salary", "cash in", "deposited", "added to").any(normalized::contains) -> TransactionDirection.INCOME
            listOf("ibft", "transfer").any(normalized::contains) -> TransactionDirection.TRANSFER
            listOf("refund", "reversed", "reversal").any(normalized::contains) -> TransactionDirection.REFUND
            else -> TransactionDirection.UNKNOWN
        }
    }

    private fun inferMerchant(text: String, direction: TransactionDirection): String {
        val compact = text.replace(Regex("""\s+"""), " ").trim()
        val patterns = when (direction) {
            TransactionDirection.INCOME -> listOf(
                Regex("""(?i)received from ([A-Za-z0-9 ._-]+?)(?: via|\.|,| on | ref| txn|$)"""),
                Regex("""(?i)credited from ([A-Za-z0-9 ._-]+?)(?: via|\.|,| on | ref| txn|$)""")
            )
            else -> listOf(
                // Specific patterns for Easypaisa, JazzCash, Foodpanda, Careem
                Regex("""(?i)sent to ([A-Za-z0-9 ._-]+?)(?: via|\.|,| on | ref| txn|$)"""),
                Regex("""(?i)paid to ([A-Za-z0-9 ._-]+?)(?: via|\.|,| on | ref| txn|$)"""),
                Regex("""(?i)(?:to|at|for) ([A-Za-z0-9 ._-]+?)(?: via|\.|,| on | ref| txn|$)"""),
                Regex("""(?i)merchant ([A-Za-z0-9 ._-]+?)(?: via|\.|,| on | ref| txn|$)""")
            )
        }

        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(compact)?.groupValues?.getOrNull(1)?.trim()
        }?.takeIf { it.isNotBlank() } ?: "Unknown merchant"
    }

    private fun confidenceFor(text: String, direction: TransactionDirection): Float {
        val hasProviderHint = listOf("easypaisa", "jazzcash", "hbl", "meezan", "debit", "credit", "pos", "ibft")
            .any { text.contains(it, ignoreCase = true) }
        return when {
            direction == TransactionDirection.UNKNOWN -> 0.55f
            hasProviderHint -> 0.9f
            else -> 0.76f
        }
    }
}
