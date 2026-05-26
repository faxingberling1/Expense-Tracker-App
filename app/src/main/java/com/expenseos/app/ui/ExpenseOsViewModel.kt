package com.expenseos.app.ui

import androidx.lifecycle.ViewModel
import com.expenseos.app.core.model.Category
import com.expenseos.app.core.model.FinancialHealth
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import com.expenseos.app.core.model.TransactionStatus
import com.expenseos.app.core.model.UdhaarEntry
import com.expenseos.app.core.model.UdhaarType
import com.expenseos.app.features.parsing.SmsTransactionParser
import com.expenseos.app.features.parsing.VoiceTransactionParser
import com.expenseos.app.features.rewards.RewardEngine
import com.expenseos.app.features.rewards.RewardEvent
import com.expenseos.app.features.rewards.RewardEventType
import com.expenseos.app.features.rewards.RewardProfile
import com.expenseos.app.features.home.HomeTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZonedDateTime
import java.util.UUID

enum class UdhaarFilter {
    GIVEN,    // Money you gave (others owe you)
    RECEIVED  // Money you received (you owe others)
}

data class ExpenseOsUiState(
    val candidates: List<TransactionCandidate> = emptyList(),
    val health: FinancialHealth = FinancialHealth(0, "Loading", "Preparing your money timeline."),
    val selectedHomeTab: HomeTab = HomeTab.Timeline,
    val udhaarEntries: List<UdhaarEntry> = emptyList(),
    val selectedUdhaarTab: UdhaarFilter = UdhaarFilter.GIVEN,
    val rewardProfile: RewardProfile = RewardProfile("user-123", 280, listOf("First Step")),
    val recentRewardEvents: List<RewardEvent> = emptyList(),
    val isPremiumUser: Boolean = false,
    val lastNotificationMessage: String? = null
)

class ExpenseOsViewModel : ViewModel() {
    private val smsParser = SmsTransactionParser()
    private val voiceParser = VoiceTransactionParser()
    private val rewardEngine = RewardEngine()

    private val _uiState = MutableStateFlow(ExpenseOsUiState())
    val uiState: StateFlow<ExpenseOsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Initial verified and suggested transactions
        val now = ZonedDateTime.now()
        val initialCandidates = listOf(
            // Verified (Shown in Timeline)
            TransactionCandidate(
                id = UUID.randomUUID().toString(),
                amount = 2450,
                direction = TransactionDirection.EXPENSE,
                merchant = "Foodpanda",
                category = Category.FOOD,
                source = TransactionSource.SMS,
                occurredAt = now.minusHours(4),
                confidence = 0.95f,
                rawText = "HBL Alert: PKR 2,450 debited at Foodpanda",
                status = TransactionStatus.CONFIRMED
            ),
            TransactionCandidate(
                id = UUID.randomUUID().toString(),
                amount = 1200,
                direction = TransactionDirection.EXPENSE,
                merchant = "Shell Fuel",
                category = Category.FUEL,
                source = TransactionSource.SMS,
                occurredAt = now.minusDays(1),
                confidence = 0.9f,
                rawText = "Easypaisa: You have sent Rs. 1,200 to Shell Fuel.",
                status = TransactionStatus.CONFIRMED
            ),
            TransactionCandidate(
                id = UUID.randomUUID().toString(),
                amount = 15000,
                direction = TransactionDirection.INCOME,
                merchant = "Ali Khan",
                category = Category.INCOME,
                source = TransactionSource.SMS,
                occurredAt = now.minusDays(2),
                confidence = 0.92f,
                rawText = "JazzCash: You received Rs. 15,000 from Ali Khan.",
                status = TransactionStatus.CONFIRMED
            ),
            // Suggested (Shown in Inbox initially)
            TransactionCandidate(
                id = "suggested-1",
                amount = 850,
                direction = TransactionDirection.EXPENSE,
                merchant = "Lunch",
                category = Category.FOOD,
                source = TransactionSource.VOICE,
                occurredAt = now.minusHours(1),
                confidence = 0.82f,
                rawText = "Spent 850 on lunch",
                status = TransactionStatus.SUGGESTED
            ),
            TransactionCandidate(
                id = "suggested-2",
                amount = 2500,
                direction = TransactionDirection.EXPENSE,
                merchant = "Groceries",
                category = Category.GROCERIES,
                source = TransactionSource.VOICE,
                occurredAt = now.minusHours(2),
                confidence = 0.85f,
                rawText = "Kal 2500 groceries pe kharch kiye",
                status = TransactionStatus.SUGGESTED
            )
        )

        val initialUdhaar = listOf(
            UdhaarEntry(
                contactName = "Kamran Ali",
                phoneNumber = "0300-1234567",
                amount = 2500,
                type = UdhaarType.GIVEN,
                note = "Chaye & Lunch share",
                dueDate = now.plusDays(3),
                isSettled = false,
                createdAt = now.minusDays(5)
            ),
            UdhaarEntry(
                contactName = "Zainab Shah",
                phoneNumber = "0321-7654321",
                amount = 4500,
                type = UdhaarType.RECEIVED,
                note = "Office event contribution",
                dueDate = now.plusDays(6),
                isSettled = false,
                createdAt = now.minusDays(2)
            ),
            UdhaarEntry(
                contactName = "Usman Khan",
                phoneNumber = "0333-1122334",
                amount = 1000,
                type = UdhaarType.GIVEN,
                note = "Biryani treat pool",
                dueDate = now.minusDays(1),
                isSettled = true,
                createdAt = now.minusDays(4),
                settledAt = now.minusDays(1)
            )
        )

        val initialEvents = listOf(
            RewardEvent(
                type = RewardEventType.FIRST_RECEIPT_SCANNED,
                description = "Claimed First Receipt Reward",
                points = 20,
                timestamp = now.minusDays(4)
            ),
            RewardEvent(
                type = RewardEventType.WEEKLY_BUDGET_MET,
                description = "Earned for meeting weekly savings budget",
                points = 100,
                timestamp = now.minusDays(2)
            )
        )

        _uiState.value = ExpenseOsUiState(
            candidates = initialCandidates,
            udhaarEntries = initialUdhaar,
            recentRewardEvents = initialEvents,
            rewardProfile = RewardProfile("user-123", 280, listOf("First Step"))
        ).copy(
            health = calculateFinancialHealth(initialCandidates)
        )
    }

    // --- Home Logic ---
    fun selectHomeTab(tab: HomeTab) {
        _uiState.value = _uiState.value.copy(selectedHomeTab = tab)
    }

    fun confirmCandidate(candidateId: String) {
        val currentState = _uiState.value
        val updatedCandidates = currentState.candidates.map {
            if (it.id == candidateId) it.copy(status = TransactionStatus.CONFIRMED) else it
        }

        // Award +10 coins for confirming a parsed transaction (or voice/SMS)
        val eventType = RewardEventType.MANUAL_EXPENSE_LOGGED
        val (updatedProfile, event) = rewardEngine.processEvent(
            currentState.rewardProfile,
            eventType,
            "Confirmed transaction to timeline"
        )
        val fullyCheckedProfile = rewardEngine.checkBadges(updatedProfile)

        _uiState.value = currentState.copy(
            candidates = updatedCandidates,
            health = calculateFinancialHealth(updatedCandidates),
            rewardProfile = fullyCheckedProfile,
            recentRewardEvents = listOf(event) + currentState.recentRewardEvents,
            lastNotificationMessage = "Confirmed! +10 Expense Coins earned! 🪙"
        )
    }

    fun confirmAllSuggested() {
        val currentState = _uiState.value
        val suggestedCount = currentState.candidates.count { it.status == TransactionStatus.SUGGESTED }
        if (suggestedCount == 0) return

        val updatedCandidates = currentState.candidates.map {
            if (it.status == TransactionStatus.SUGGESTED) it.copy(status = TransactionStatus.CONFIRMED) else it
        }

        val totalPointsEarned = suggestedCount * 10
        val event = RewardEvent(
            type = RewardEventType.MANUAL_EXPENSE_LOGGED,
            description = "Bulk confirmed $suggestedCount transactions",
            points = totalPointsEarned
        )
        val updatedProfile = currentState.rewardProfile.copy(
            totalCoins = currentState.rewardProfile.totalCoins + totalPointsEarned
        )
        val fullyCheckedProfile = rewardEngine.checkBadges(updatedProfile)

        _uiState.value = currentState.copy(
            candidates = updatedCandidates,
            health = calculateFinancialHealth(updatedCandidates),
            rewardProfile = fullyCheckedProfile,
            recentRewardEvents = listOf(event) + currentState.recentRewardEvents,
            lastNotificationMessage = "Confirmed $suggestedCount transactions! +$totalPointsEarned Coins earned!"
        )
    }

    fun simulateParsedText(text: String, isVoice: Boolean) {
        val candidate = if (isVoice) {
            voiceParser.parse(text)
        } else {
            smsParser.parse(text)
        }

        if (candidate != null) {
            val currentState = _uiState.value
            // Check if we already have it in suggested/verified to avoid spamming
            val alreadyExists = currentState.candidates.any { it.rawText == text }
            if (alreadyExists) return

            val updatedCandidates = listOf(candidate.copy(
                id = UUID.randomUUID().toString(),
                occurredAt = ZonedDateTime.now(),
                status = TransactionStatus.SUGGESTED
            )) + currentState.candidates

            _uiState.value = currentState.copy(
                candidates = updatedCandidates,
                selectedHomeTab = HomeTab.Inbox, // Swaps tab to Inbox so user immediately sees it
                lastNotificationMessage = "Smart intelligence identified a new transaction! Check your Inbox. 🤖"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                lastNotificationMessage = "Could not parse amount or merchant. Make sure to mention numbers! ❌"
            )
        }
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(lastNotificationMessage = null)
    }

    private fun calculateFinancialHealth(candidates: List<TransactionCandidate>): FinancialHealth {
        val verifiedExpenses = candidates
            .filter { it.status == TransactionStatus.CONFIRMED && it.direction == TransactionDirection.EXPENSE }
            .sumOf { it.amount }

        val score = when {
            verifiedExpenses < 5_000 -> 88
            verifiedExpenses < 15_000 -> 78
            verifiedExpenses < 30_000 -> 65
            else -> 49
        }

        return FinancialHealth(
            score = score,
            label = if (score >= 75) "Steady" else "Watchful",
            explanation = if (score >= 75) {
                "Excellent work. Your expenses are well within control. Keep saving!"
            } else {
                "Your food, cash, and transfers are mounting. Review recent expenses and secure your limits."
            }
        )
    }

    // --- Udhaar Logic ---
    fun selectUdhaarTab(filter: UdhaarFilter) {
        _uiState.value = _uiState.value.copy(selectedUdhaarTab = filter)
    }

    fun addUdhaarEntry(name: String, amount: Long, type: UdhaarType, note: String?) {
        val currentState = _uiState.value
        val newEntry = UdhaarEntry(
            contactName = name,
            amount = amount,
            type = type,
            note = note,
            dueDate = ZonedDateTime.now().plusDays(7),
            isSettled = false,
            createdAt = ZonedDateTime.now()
        )

        _uiState.value = currentState.copy(
            udhaarEntries = listOf(newEntry) + currentState.udhaarEntries,
            lastNotificationMessage = "Logged peer debt of PKR ${amount} for ${name}! 📝"
        )
    }

    fun settleUdhaarEntry(entryId: String) {
        val currentState = _uiState.value
        var settledAmount = 0L
        var personName = "someone"

        val updatedEntries = currentState.udhaarEntries.map {
            if (it.id == entryId) {
                settledAmount = it.amount
                personName = it.contactName
                it.copy(isSettled = true, settledAt = ZonedDateTime.now())
            } else {
                it
            }
        }

        // Award +50 coins for settling an Udhaar debt!
        val (updatedProfile, event) = rewardEngine.processEvent(
            currentState.rewardProfile,
            RewardEventType.UDHAAR_SETTLED,
            "Settled Udhaar of PKR $settledAmount with $personName"
        )
        
        // Add additional badges checking
        var checkedProfile = rewardEngine.checkBadges(updatedProfile)
        
        // Custom badge: "Udhaar Master" if they have settled 2 or more debts!
        val settledCount = updatedEntries.count { it.isSettled }
        if (settledCount >= 2 && !checkedProfile.badges.contains("Udhaar Master")) {
            val newBadges = checkedProfile.badges.toMutableList()
            newBadges.add("Udhaar Master")
            checkedProfile = checkedProfile.copy(badges = newBadges)
        }

        _uiState.value = currentState.copy(
            udhaarEntries = updatedEntries,
            rewardProfile = checkedProfile,
            recentRewardEvents = listOf(event) + currentState.recentRewardEvents,
            lastNotificationMessage = "Udhaar settled! +50 Expense Coins awarded! 🎉"
        )
    }

    // --- Profile & Premium Logic ---
    fun togglePremium() {
        val currentState = _uiState.value
        val nextPremium = !currentState.isPremiumUser
        
        // Award points on first premium trigger as easter egg
        val updatedProfile = if (nextPremium && !currentState.rewardProfile.badges.contains("Pro Club")) {
            val (prof, event) = rewardEngine.processEvent(
                currentState.rewardProfile,
                RewardEventType.WEEKLY_BUDGET_MET,
                "Joined the Premium Pro Club!"
            )
            val newBadges = prof.badges.toMutableList()
            newBadges.add("Pro Club")
            prof.copy(badges = newBadges, totalCoins = prof.totalCoins + 100)
        } else {
            currentState.rewardProfile
        }

        _uiState.value = currentState.copy(
            isPremiumUser = nextPremium,
            rewardProfile = updatedProfile,
            lastNotificationMessage = if (nextPremium) {
                "Welcome to Expense OS Pro! Full access unlocked! ✨"
            } else {
                "Returned to Standard Edition."
            }
        )
    }
}
