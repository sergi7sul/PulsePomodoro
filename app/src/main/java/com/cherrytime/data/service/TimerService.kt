package com.cherrytime.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.cherrytime.MainActivity
import com.cherrytime.data.notification.NotificationHelper
import com.cherrytime.data.worker.BreakReminderWorker
import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private val binder = TimerBinder()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val phase = intent.getStringExtra(EXTRA_PHASE)
                    ?.let { Phase.valueOf(it) } ?: Phase.WORK
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, phase.defaultDurationMs)
                startSession(phase, durationMs)
            }
            ACTION_PAUSE -> pauseSession()
            ACTION_RESUME -> resumeSession()
            ACTION_RESET -> resetSession()
            ACTION_SKIP -> skipSession()
        }
        return START_STICKY
    }

    fun startSession(phase: Phase, durationMs: Long = phase.defaultDurationMs) {
        cancelBreakReminder()
        startCountdown(phase, durationMs, durationMs)
    }

    fun pauseSession() {
        val current = _timerState.value as? TimerState.Running ?: return
        timerJob?.cancel()
        _timerState.value = TimerState.Paused(current.remainingMs, current.phase, current.totalMs)
        updateNotification()
    }

    fun resumeSession() {
        val current = _timerState.value as? TimerState.Paused ?: return
        startCountdown(current.phase, current.totalMs, current.remainingMs)
    }

    fun resetSession() {
        timerJob?.cancel()
        _timerState.value = TimerState.Idle
        cancelBreakReminder()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun skipSession() {
        val phase = when (val s = _timerState.value) {
            is TimerState.Running -> s.phase
            is TimerState.Paused -> s.phase
            else -> return
        }
        timerJob?.cancel()
        onSessionComplete(phase)
    }

    private fun startCountdown(phase: Phase, totalMs: Long, startingFromMs: Long) {
        timerJob?.cancel()
        val endTime = System.currentTimeMillis() + startingFromMs

        val notification = buildNotification(startingFromMs, phase, isPaused = false)
        startForeground(NotificationHelper.NOTIFICATION_ID_TIMER, notification)

        timerJob = serviceScope.launch {
            while (true) {
                val remaining = (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
                _timerState.value = TimerState.Running(remaining, phase, totalMs)
                updateNotification()
                if (remaining == 0L) {
                    onSessionComplete(phase)
                    break
                }
                delay(500L)
            }
        }
    }

    private fun onSessionComplete(phase: Phase) {
        _timerState.value = TimerState.Finished(phase)
        stopForeground(STOP_FOREGROUND_REMOVE)
        scheduleBreakReminder(phase)
        stopSelf()
    }

    private fun updateNotification() {
        val notification = when (val s = _timerState.value) {
            is TimerState.Running -> buildNotification(s.remainingMs, s.phase, isPaused = false)
            is TimerState.Paused -> buildNotification(s.remainingMs, s.phase, isPaused = true)
            else -> return
        }
        getSystemService(NotificationManager::class.java)
            .notify(NotificationHelper.NOTIFICATION_ID_TIMER, notification)
    }

    private fun buildNotification(remainingMs: Long, phase: Phase, isPaused: Boolean) =
        notificationHelper.buildTimerNotification(
            remainingMs = remainingMs,
            phase = phase,
            isPaused = isPaused,
            openAppIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
            toggleIntent = PendingIntent.getService(
                this, 1,
                Intent(this, TimerService::class.java).apply {
                    action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )

    private fun scheduleBreakReminder(completedPhase: Phase) {
        val message = if (completedPhase == Phase.WORK) "Time for a break! 🍒" else "Ready to focus again?"
        val request = OneTimeWorkRequestBuilder<BreakReminderWorker>()
            .setInputData(workDataOf(BreakReminderWorker.KEY_MESSAGE to message))
            .setInitialDelay(2, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            BreakReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun cancelBreakReminder() {
        WorkManager.getInstance(applicationContext)
            .cancelUniqueWork(BreakReminderWorker.WORK_NAME)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.cherrytime.timer.START"
        const val ACTION_PAUSE = "com.cherrytime.timer.PAUSE"
        const val ACTION_RESUME = "com.cherrytime.timer.RESUME"
        const val ACTION_RESET = "com.cherrytime.timer.RESET"
        const val ACTION_SKIP = "com.cherrytime.timer.SKIP"

        const val EXTRA_PHASE = "extra_phase"
        const val EXTRA_DURATION_MS = "extra_duration_ms"
    }
}
