package com.expenseos.app.features.rewards

import java.time.ZonedDateTime
import java.util.UUID

enum class RewardEventType(val pointsAwarded: Int) {
    MANUAL_EXPENSE_LOGGED(10),
    UDHAAR_SETTLED(50),
    WEEKLY_BUDGET_MET(100),
    FIRST_RECEIPT_SCANNED(20)
}

data class RewardEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: RewardEventType,
    val points: Int = type.pointsAwarded,
    val description: String,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
)

data class RewardProfile(
    val userId: String,
    val totalCoins: Int = 0,
    val badges: List<String> = emptyList()
)

class RewardEngine {
    
    fun processEvent(profile: RewardProfile, type: RewardEventType, customDescription: String? = null): Pair<RewardProfile, RewardEvent> {
        val event = RewardEvent(
            type = type,
            description = customDescription ?: "Earned points for ${type.name.lowercase().replace("_", " ")}"
        )
        
        val updatedProfile = profile.copy(
            totalCoins = profile.totalCoins + event.points
        )
        
        return Pair(updatedProfile, event)
    }

    fun checkBadges(profile: RewardProfile): RewardProfile {
        val newBadges = profile.badges.toMutableList()
        var changed = false

        if (profile.totalCoins >= 1000 && !newBadges.contains("The Saver")) {
            newBadges.add("The Saver")
            changed = true
        }

        return if (changed) profile.copy(badges = newBadges) else profile
    }
}
