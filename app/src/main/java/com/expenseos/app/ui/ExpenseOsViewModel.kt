package com.expenseos.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expenseos.app.core.db.AppRepository
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
import com.expenseos.app.features.insights.InsightCardModel
import com.expenseos.app.features.insights.InsightsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.UUID

enum class UdhaarFilter {
    GIVEN,
    RECEIVED
}

data class ExpenseOsUiState(
    val candidates: List<TransactionCandidate> = emptyList(),
    val health: FinancialHealth = FinancialHealth(0, "Loading", "Preparing your money timeline."),
    val insights: List<InsightCardModel> = emptyList(),
    val selectedHomeTab: HomeTab = HomeTab.Timeline,
    val udhaarEntries: List<UdhaarEntry> = emptyList(),
    val selectedUdhaarTab: UdhaarFilter = UdhaarFilter.GIVEN,
    val rewardProfile: RewardProfile = RewardProfile("user-123", 0, emptyList()),
    val recentRewardEvents: List<RewardEvent> = emptyList(),
    val isPremiumUser: Boolean = false,
    val lastNotificationMessage: String? = null
)

class ExpenseOsViewModel(private val repository: AppRepository) : ViewModel() {
    private val smsParser = SmsTransactionParser()
    private val voiceParser = VoiceTransactionParser()
    private val rewardEngine = RewardEngine()

    private val _uiState = MutableStateFlow(ExpenseOsUiState())
    val uiState: StateFlow<ExpenseOsUiState> = _uiState.asStateFlow()

    init {
        // We will seed initial data if DB is empty, otherwise observe
        viewModelScope.launch {
            seedDatabaseIfNeeded()
            observeDatabase()
        }
    }

    private suspend fun seedDatabaseIfNeeded() {
        // Add sample data if needed (Skipped for brevity in DB migration unless specifically requested)
        // Usually checked via querying count
    }

    private val insightsEngine = InsightsEngine()

    private fun observeDatabase() {
        viewModelScope.launch {
            combine(
                repository.observeAllTransactions(),
                repository.observeActiveUdhaar(),
                repository.observeTotalPoints()
            ) { transactions, udhaar, points ->
                val profile = RewardProfile("user-123", points, listOf("First Step"))
                val checkedProfile = rewardEngine.checkBadges(profile)

                val generatedInsights = insightsEngine.generateInsights(transactions)

                _uiState.value.copy(
                    candidates = transactions,
                    udhaarEntries = udhaar,
                    rewardProfile = checkedProfile,
                    health = calculateFinancialHealth(transactions),
                    insights = generatedInsights
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    // --- Home Logic ---
    fun selectHomeTab(tab: HomeTab) {
        _uiState.value = _uiState.value.copy(selectedHomeTab = tab)
    }

    fun confirmCandidate(candidateId: String) {
        viewModelScope.launch {
            repository.updateTransactionStatus(candidateId, TransactionStatus.CONFIRMED)
            repository.addRewardEvent(RewardEventType.MANUAL_EXPENSE_LOGGED, 10, "Confirmed transaction to timeline")
            _uiState.value = _uiState.value.copy(
                lastNotificationMessage = "Confirmed! +10 Expense Coins earned! 🪙"
            )
        }
    }

    fun confirmAllSuggested() {
        val suggestedCount = _uiState.value.candidates.count { it.status == TransactionStatus.SUGGESTED }
        if (suggestedCount == 0) return

        viewModelScope.launch {
            _uiState.value.candidates.filter { it.status == TransactionStatus.SUGGESTED }.forEach {
                repository.updateTransactionStatus(it.id, TransactionStatus.CONFIRMED)
            }
            val totalPointsEarned = suggestedCount * 10
            repository.addRewardEvent(RewardEventType.MANUAL_EXPENSE_LOGGED, totalPointsEarned, "Bulk confirmed $suggestedCount transactions")
            
            _uiState.value = _uiState.value.copy(
                lastNotificationMessage = "Confirmed $suggestedCount transactions! +$totalPointsEarned Coins earned!"
            )
        }
    }

    fun simulateParsedText(text: String, isVoice: Boolean) {
        val candidate = if (isVoice) {
            voiceParser.parse(text)
        } else {
            smsParser.parse(text)
        }

        if (candidate != null) {
            viewModelScope.launch {
                val newCandidate = candidate.copy(
                    id = UUID.randomUUID().toString(),
                    occurredAt = ZonedDateTime.now(),
                    status = TransactionStatus.SUGGESTED
                )
                repository.insertTransaction(newCandidate)
                _uiState.value = _uiState.value.copy(
                    selectedHomeTab = HomeTab.Inbox,
                    lastNotificationMessage = "Smart intelligence identified a new transaction! Check your Inbox. 🤖"
                )
            }
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
        viewModelScope.launch {
            val newEntry = UdhaarEntry(
                id = UUID.randomUUID().toString(),
                contactName = name,
                phoneNumber = null,
                amount = amount,
                type = type,
                note = note,
                dueDate = ZonedDateTime.now().plusDays(7),
                isSettled = false,
                createdAt = ZonedDateTime.now()
            )
            repository.insertUdhaar(newEntry)
            _uiState.value = _uiState.value.copy(
                lastNotificationMessage = "Logged peer debt of PKR ${amount} for ${name}! 📝"
            )
        }
    }

    fun settleUdhaarEntry(entryId: String) {
        viewModelScope.launch {
            repository.markUdhaarSettled(entryId)
            repository.addRewardEvent(RewardEventType.UDHAAR_SETTLED, 50, "Settled Udhaar")
            
            _uiState.value = _uiState.value.copy(
                lastNotificationMessage = "Udhaar settled! +50 Expense Coins awarded! 🎉"
            )
        }
    }

    // --- Profile & Premium Logic ---
    fun togglePremium() {
        val nextPremium = !_uiState.value.isPremiumUser
        _uiState.value = _uiState.value.copy(
            isPremiumUser = nextPremium,
            lastNotificationMessage = if (nextPremium) {
                "Welcome to Expense OS Pro! Full access unlocked! ✨"
            } else {
                "Returned to Standard Edition."
            }
        )
    }
}

class ExpenseOsViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseOsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseOsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
