package com.cherrytime.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.data.datastore.UserPreferences
import com.cherrytime.data.datastore.UserPreferencesRepository
import com.cherrytime.data.remote.GeminiCoachingService
import com.cherrytime.data.remote.GeminiKeyStore
import com.cherrytime.domain.usecase.ScheduleRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val scheduleReminders: ScheduleRemindersUseCase,
    private val geminiKeyStore: GeminiKeyStore,
    private val geminiService: GeminiCoachingService,
) : ViewModel() {

    private val _geminiKeyState = MutableStateFlow(GeminiKeyState(
        hasKey = geminiKeyStore.getKey() != null,
    ))
    val geminiKeyState: StateFlow<GeminiKeyState> = _geminiKeyState.asStateFlow()

    fun saveGeminiKey(key: String) = viewModelScope.launch {
        _geminiKeyState.value = _geminiKeyState.value.copy(isValidating = true, error = null)
        val valid = geminiService.validateKey(key)
        if (valid) {
            geminiKeyStore.setKey(key)
            _geminiKeyState.value = GeminiKeyState(hasKey = true)
        } else {
            _geminiKeyState.value = GeminiKeyState(hasKey = false, error = "Invalid key or network error")
        }
    }

    fun clearGeminiKey() {
        geminiKeyStore.clearKey()
        _geminiKeyState.value = GeminiKeyState(hasKey = false)
    }

    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setWorkDuration(m: Int) = viewModelScope.launch { repository.setWorkDuration(m) }
    fun setShortBreak(m: Int) = viewModelScope.launch { repository.setShortBreak(m) }
    fun setLongBreak(m: Int) = viewModelScope.launch { repository.setLongBreak(m) }
    fun setLongBreakInterval(n: Int) = viewModelScope.launch { repository.setLongBreakInterval(n) }
    fun setSoundEnabled(v: Boolean) = viewModelScope.launch { repository.setSoundEnabled(v) }
    fun setVibrationEnabled(v: Boolean) = viewModelScope.launch { repository.setVibrationEnabled(v) }

    fun setWaterReminderEnabled(v: Boolean) = viewModelScope.launch {
        repository.setWaterReminderEnabled(v)
        scheduleReminders.reschedule()
    }

    fun setWaterReminderInterval(m: Int) = viewModelScope.launch {
        repository.setWaterReminderInterval(m)
        scheduleReminders.reschedule()
    }

    fun setPostureReminderEnabled(v: Boolean) = viewModelScope.launch {
        repository.setPostureReminderEnabled(v)
        scheduleReminders.reschedule()
    }

    fun setPostureReminderInterval(m: Int) = viewModelScope.launch {
        repository.setPostureReminderInterval(m)
        scheduleReminders.reschedule()
    }
}
