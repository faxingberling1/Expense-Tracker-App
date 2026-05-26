package com.expenseos.app.core.model

import java.time.ZonedDateTime
import java.util.UUID

data class TransactionCandidate(
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,
    val currencyCode: String = "PKR",
    val direction: TransactionDirection,
    val merchant: String,
    val category: Category,
    val source: TransactionSource,
    val occurredAt: ZonedDateTime,
    val confidence: Float,
    val rawText: String,
    val status: TransactionStatus = TransactionStatus.SUGGESTED
)

