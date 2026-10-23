package com.emathias.periodic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.notification.NotificationHelper
import com.emathias.periodic.notification.NotificationScheduler
import com.emathias.periodic.service.ScheduledItemProcessor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class DailyNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduledItemProcessor: ScheduledItemProcessor,
    private val todoItemDao: TodoItemDao,
    private val notificationHelper: NotificationHelper,
    private val notificationScheduler: NotificationScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val action = inputData.getString(NotificationScheduler.ACTION_KEY)
            ?: NotificationScheduler.ACTION_NOTIFY
        try {
            when (action) {
                NotificationScheduler.ACTION_PROCESS -> scheduledItemProcessor.processAll()
                NotificationScheduler.ACTION_NOTIFY -> notificationHelper.notifySummary(
                    todoItemDao.countOpen(),
                    todoItemDao.countOverdue(LocalDate.now().toEpochDay()),
                )
                else -> return Result.failure()
            }
        } finally {
            when (action) {
                NotificationScheduler.ACTION_PROCESS ->
                    notificationScheduler.scheduleProcessing(replace = true)
                NotificationScheduler.ACTION_NOTIFY ->
                    notificationScheduler.scheduleNotification(replace = true)
                else -> Unit
            }
        }
        return Result.success()
    }
}
