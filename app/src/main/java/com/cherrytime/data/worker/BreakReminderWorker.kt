package com.cherrytime.data.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cherrytime.MainActivity
import com.cherrytime.data.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BreakReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val message = inputData.getString(KEY_MESSAGE) ?: "Time for your next session!"
        val openAppIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = notificationHelper.buildReminderNotification(message, openAppIntent)
        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(NotificationHelper.NOTIFICATION_ID_REMINDER, notification)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "break_reminder"
        const val KEY_MESSAGE = "key_message"
    }
}
