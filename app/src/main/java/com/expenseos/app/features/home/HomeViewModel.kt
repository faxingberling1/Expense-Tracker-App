package com.expenseos.app.features.home

import androidx.lifecycle.ViewModel
import com.expenseos.app.core.model.FinancialHealth
import com.expenseos.app.core.model.TransactionCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val candidates: List<TransactionCandidate> = emptyList(),
    val health: FinancialHealth = FinancialHealth(0, "Loading", "Preparing your money timeline."),
    val selectedTab: HomeTab = HomeTab.Timeline
)

enum class HomeTab {
    Timeline,
    Inbox,
    Rewind
}

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val candidates = repository.loadCandidates()
        _uiState.value = HomeUiState(
            candidates = candidates,
            health = repository.financialHealth(candidates)
        )
    }

    fun selectTab(tab: HomeTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}

