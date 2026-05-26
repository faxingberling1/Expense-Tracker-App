package com.expenseos.app.core.model

import java.time.ZonedDateTime
import java.util.UUID

enum class UdhaarType {
    GIVEN,    // Money you gave to someone else (they owe you)
    RECEIVED  // Money you received from someone else (you owe them)
}

data class UdhaarEntry(
    val id: String = UUID.randomUUID().toString(),
    val contactName: String,
    val phoneNumber: String? = null,
    val amount: Long,
    val type: UdhaarType,
    val note: String? = null,
    val dueDate: ZonedDateTime? = null,
    val isSettled: Boolean = false,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val settledAt: ZonedDateTime? = null
)
