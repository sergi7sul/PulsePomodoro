package com.cherrytime.feature.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.data.datastore.UserPreferences
import com.cherrytime.data.datastore.UserPreferencesRepository
import com.cherrytime.data.service.TimerService
import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private var serviceBound = false

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as TimerService.TimerBinder).getService()
            serviceBound = true
            viewModelScope.launch {
                service.timerState.collect { state -> updateUiState(state) }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    init {
        context.bindService(
            Intent(context, TimerService::class.java),
            connection,
            Context.BIND_AUTO_CREATE,
        )
    }

    fun onIntent(intent: TimerIntent) {
        val svcIntent = Intent(context, TimerService::class.java)
        when (intent) {
            TimerIntent.Start -> {
                val phase = _uiState.value.nextPhase
                val durationMs = preferences.value.durationMs(phase)
                svcIntent.action = TimerService.ACTION_START
                svcIntent.putExtra(TimerService.EXTRA_PHASE, phase.name)
                svcIntent.putExtra(TimerService.EXTRA_DURATION_MS, durationMs)
                context.startForegroundService(svcIntent)
            }
            TimerIntent.Pause -> {
                svcIntent.action = TimerService.ACTION_PAUSE
                context.startService(svcIntent)
            }
            TimerIntent.Resume -> {
                svcIntent.action = TimerService.ACTION_RESUME
                context.startService(svcIntent)
            }
            TimerIntent.Reset -> {
                svcIntent.action = TimerService.ACTION_RESET
                context.startService(svcIntent)
            }
            TimerIntent.Skip -> {
                svcIntent.action = TimerService.ACTION_SKIP
                context.startService(svcIntent)
            }
        }
    }

    private fun updateUiState(state: TimerState) {
        val current = _uiState.value
        val interval = preferences.value.longBreakInterval
        val (completedSessions, nextPhase) = when {
            state is TimerState.Finished && state.phase == Phase.WORK -> {
                val sessions = current.completedWorkSessions + 1
                sessions to nextBreakPhase(sessions, interval)
            }
            state is TimerState.Finished -> current.completedWorkSessions to Phase.WORK
            else -> current.completedWorkSessions to current.nextPhase
        }
        _uiState.value = TimerUiState(
            timerState = state,
            nextPhase = nextPhase,
            completedWorkSessions = completedSessions,
        )
    }

    private fun nextBreakPhase(completedWorkSessions: Int, interval: Int): Phase =
        if (completedWorkSessions % interval == 0) Phase.LONG_BREAK else Phase.SHORT_BREAK

    override fun onCleared() {
        if (serviceBound) context.unbindService(connection)
        super.onCleared()
    }
}
