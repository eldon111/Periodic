package com.emathias.periodic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.emathias.periodic.db.dao.ScheduledItemProcessHistoryDao
import com.emathias.periodic.service.ScheduledItemProcessor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant

@HiltWorker
class ScheduledItemWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    val scheduledItemProcessHistoryDao: ScheduledItemProcessHistoryDao,
    val scheduledItemProcessor: ScheduledItemProcessor,
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val cutoff = scheduledItemProcessHistoryDao
            .getMostRecent()?.processedAt
            ?: Instant.MIN

        scheduledItemProcessor.processAll(cutoff)

        return Result.success()
    }
}