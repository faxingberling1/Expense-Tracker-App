package com.expenseos.app.core.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "udhaar_entries")
data class UdhaarEntity(
    @PrimaryKey val id: String,
    val contactName: String,
    val phoneNumber: String?,
    val amount: Long,
    val type: String,           // UdhaarType.name
    val note: String?,
    val dueDate: Long?,         // epoch millis, nullable
    val isSettled: Boolean,
    val createdAt: Long,        // epoch millis
    val settledAt: Long?        // epoch millis, nullable
)
