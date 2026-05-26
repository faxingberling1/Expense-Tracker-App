package com.expenseos.app.core.model

import java.time.YearMonth

data class BudgetEnvelope(
    val id: String,
    val category: Category,
    val limitAmount: Long,
    val spentAmount: Long = 0L,
    val month: YearMonth
) {
    val remainingAmount: Long
        get() = maxOf(0L, limitAmount - spentAmount)
        
    val isOverBudget: Boolean
        get() = spentAmount >= limitAmount
        
    val progress: Float
        get() = if (limitAmount > 0) (spentAmount.toFloat() / limitAmount.toFloat()).coerceIn(0f, 1f) else 0f
}
