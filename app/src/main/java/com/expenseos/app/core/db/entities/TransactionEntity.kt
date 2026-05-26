package com.expenseos.app.core.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Long,
    val currencyCode: String,
    val direction: String,       // TransactionDirection.name
    val merchant: String,
    val category: String,        // Category.name
    val source: String,          // TransactionSource.name
    val occurredAt: Long,        // epoch millis
    val confidence: Float,
    val rawText: String,
    val status: String           // TransactionStatus.name
)
