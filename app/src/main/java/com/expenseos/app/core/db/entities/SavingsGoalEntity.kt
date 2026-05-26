package com.expenseos.app.core.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val emoji: String,
    val targetAmount: Long,
    val savedAmount: Long,
    val targetDate: Long?,
    val isCompleted: Boolean,
    val createdAt: Long
)
