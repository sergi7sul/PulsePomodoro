package com.cherrytime.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.data.datastore.UserPreferences
import com.cherrytime.data.datastore.UserPreferencesRepository
import com.cherrytime.domain.usecase.ScheduleRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val scheduleReminders: ScheduleRemindersUseCase,
) : ViewModel() {

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
