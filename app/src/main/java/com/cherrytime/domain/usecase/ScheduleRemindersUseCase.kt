package com.cherrytime.domain.usecase

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cherrytime.data.datastore.UserPreferencesRepository
import com.cherrytime.data.worker.PostureReminderWorker
import com.cherrytime.data.worker.WaterReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleRemindersUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: UserPreferencesRepository,
) {
    suspend fun reschedule() {
        val prefs = repository.preferences.first()
        val workManager = WorkManager.getInstance(context)

        if (prefs.waterReminderEnabled) {
            val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
                prefs.waterReminderIntervalMin.toLong(), TimeUnit.MINUTES,
            ).build()
            workManager.enqueueUniquePeriodicWork(
                WaterReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        } else {
            workManager.cancelUniqueWork(WaterReminderWorker.WORK_NAME)
        }

        if (prefs.postureReminderEnabled) {
            val request = PeriodicWorkRequestBuilder<PostureReminderWorker>(
                prefs.postureReminderIntervalMin.toLong(), TimeUnit.MINUTES,
            ).build()
            workManager.enqueueUniquePeriodicWork(
                PostureReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        } else {
            workManager.cancelUniqueWork(PostureReminderWorker.WORK_NAME)
        }
    }
}
