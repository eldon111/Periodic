package com.emathias.periodic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.notification.NotificationHelper
import com.emathias.periodic.notification.NotificationScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailyNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduledItemDao: ScheduledItemDao,
    private val notificationHelper: NotificationHelper,
    private val notificationScheduler: NotificationScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            val due = scheduledItemDao.getAll().first().filter {
                NotificationHelper.isDueOn(it, today)
            }
            notificationHelper.notify(due)
        } finally {
            // Always queue tomorrow's run, even if this one failed. Must replace: this worker
            // is still running, so KEEP would treat it as the existing work and enqueue nothing.
            notificationScheduler.schedule(replace = true)
        }
        return Result.success()
    }
}
