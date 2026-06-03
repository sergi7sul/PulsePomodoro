package com.cherrytime.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.content.Context
import com.cherrytime.R
import com.cherrytime.domain.model.Phase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_TIMER = "cherrytime_timer"
        const val CHANNEL_REMINDER = "cherrytime_reminder"
        const val NOTIFICATION_ID_TIMER = 1
        const val NOTIFICATION_ID_REMINDER = 2
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)

        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            context.getString(R.string.notification_channel_timer),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_timer_desc)
            setSound(null, null)
        }

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_reminders_desc)
        }

        manager.createNotificationChannel(timerChannel)
        manager.createNotificationChannel(reminderChannel)
    }

    fun buildTimerNotification(
        remainingMs: Long,
        phase: Phase,
        isPaused: Boolean,
        openAppIntent: PendingIntent,
        toggleIntent: PendingIntent,
    ): Notification {
        val minutes = remainingMs / 60_000
        val seconds = (remainingMs % 60_000) / 1_000
        val timeText = "%02d:%02d".format(minutes, seconds)
        val statusText = if (isPaused) "Paused · $timeText" else timeText

        return NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(phase.label)
            .setContentText(statusText)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play
                else android.R.drawable.ic_media_pause,
                if (isPaused) context.getString(R.string.btn_start)
                else context.getString(R.string.btn_pause),
                toggleIntent,
            )
            .build()
    }

    fun buildReminderNotification(
        message: String,
        openAppIntent: PendingIntent,
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .build()
}
