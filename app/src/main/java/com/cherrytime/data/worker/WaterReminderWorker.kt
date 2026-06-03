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
class WaterReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val openIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = notificationHelper.buildReminderNotification(
            message = applicationContext.getString(
                com.cherrytime.R.string.notification_water_text
            ),
            openAppIntent = openIntent,
        )
        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(NotificationHelper.NOTIFICATION_ID_WATER, notification)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "water_reminder"
    }
}
