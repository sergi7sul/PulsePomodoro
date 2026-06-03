package com.cherrytime.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.domain.usecase.AnalyticsStats
import com.cherrytime.domain.usecase.GetDailyStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    getDailyStats: GetDailyStatsUseCase,
) : ViewModel() {

    val stats: StateFlow<AnalyticsStats?> = getDailyStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
