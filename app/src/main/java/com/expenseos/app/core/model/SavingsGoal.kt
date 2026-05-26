package com.expenseos.app.core.model

import java.time.ZonedDateTime

data class SavingsGoal(
    val id: String,
    val title: String,
    val emoji: String,
    val targetAmount: Long,
    val savedAmount: Long = 0L,
    val targetDate: ZonedDateTime?,
    val isCompleted: Boolean = false,
    val createdAt: ZonedDateTime
) {
    val progress: Float
        get() = if (targetAmount > 0) (savedAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
        
    val remainingAmount: Long
        get() = maxOf(0L, targetAmount - savedAmount)
}
