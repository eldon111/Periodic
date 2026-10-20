package com.emathias.periodic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emathias.periodic.db.dao.ScheduledItemProcessHistoryDao
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.notification.NotificationHelper
import com.emathias.periodic.service.ScheduledItemProcessor
import com.emathias.periodic.notification.NotificationScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate

@HiltWorker
class DailyNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduledItemProcessHistoryDao: ScheduledItemProcessHistoryDao,
    private val scheduledItemProcessor: ScheduledItemProcessor,
    private val todoItemDao: TodoItemDao,
    private val notificationHelper: NotificationHelper,
    private val notificationScheduler: NotificationScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val cutoff = scheduledItemProcessHistoryDao.getMostRecent()?.processedAt ?: Instant.MIN
            scheduledItemProcessor.processAll(cutoff)
            notificationHelper.notifySummary(
                todoItemDao.countOpen(),
                todoItemDao.countOverdue(LocalDate.now().toEpochDay()),
            )
        } finally {
            // Always queue tomorrow's run, even if this one failed. Must replace: this worker
            // is still running, so KEEP would treat it as the existing work and enqueue nothing.
            notificationScheduler.schedule(replace = true)
        }
        return Result.success()
    }
}
