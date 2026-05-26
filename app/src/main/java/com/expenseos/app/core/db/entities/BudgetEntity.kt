package com.expenseos.app.core.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expenseos.app.core.model.Category

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val limitAmount: Long,
    val spentAmount: Long,
    val monthYear: String // Format: "YYYY-MM"
)
