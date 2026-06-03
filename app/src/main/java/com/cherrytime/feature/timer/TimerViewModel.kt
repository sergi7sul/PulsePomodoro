package com.cherrytime.feature.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.data.service.TimerService
import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    private var serviceBound = false

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

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
                svcIntent.action = TimerService.ACTION_START
                svcIntent.putExtra(TimerService.EXTRA_PHASE, _uiState.value.nextPhase.name)
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
        val (completedSessions, nextPhase) = when {
            state is TimerState.Finished && state.phase == Phase.WORK -> {
                val sessions = current.completedWorkSessions + 1
                sessions to nextBreakPhase(sessions)
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

    private fun nextBreakPhase(completedWorkSessions: Int): Phase =
        if (completedWorkSessions % 4 == 0) Phase.LONG_BREAK else Phase.SHORT_BREAK

    override fun onCleared() {
        if (serviceBound) context.unbindService(connection)
        super.onCleared()
    }
}
