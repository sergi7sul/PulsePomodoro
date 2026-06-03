package com.cherrytime.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.data.datastore.UserPreferences
import com.cherrytime.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setWorkDuration(minutes: Int) = viewModelScope.launch { repository.setWorkDuration(minutes) }
    fun setShortBreak(minutes: Int) = viewModelScope.launch { repository.setShortBreak(minutes) }
    fun setLongBreak(minutes: Int) = viewModelScope.launch { repository.setLongBreak(minutes) }
    fun setLongBreakInterval(sessions: Int) = viewModelScope.launch { repository.setLongBreakInterval(sessions) }
    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { repository.setSoundEnabled(enabled) }
    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch { repository.setVibrationEnabled(enabled) }
}
