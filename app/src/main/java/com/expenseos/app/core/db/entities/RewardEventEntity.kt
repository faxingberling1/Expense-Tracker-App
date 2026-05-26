package com.expenseos.app.core.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_events")
data class RewardEventEntity(
    @PrimaryKey val id: String,
    val eventType: String,      // RewardEventType.name
    val points: Int,
    val description: String,
    val timestamp: Long         // epoch millis
)
