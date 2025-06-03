package com.emathias.periodic.service

import com.cronutils.model.time.ExecutionTime
import com.emathias.periodic.api.ScheduledItemApiService
import com.emathias.periodic.db.dao.ScheduledItemHistoryDao
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.ScheduledItemHistory
import com.emathias.periodic.db.entities.TodoItem
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemProcessor @Inject constructor(
    val scheduledItemApiService: ScheduledItemApiService,
    val scheduledItemHistoryDao: ScheduledItemHistoryDao,
    val todoItemDao: TodoItemDao,
) {

    suspend fun processAll(since: Instant) {
        scheduledItemApiService.getAllScheduledItems().collect { items ->
            items.forEach { item ->
                println("Processing scheduled item '${item.description}'")
                process(item, since)
            }
        }
    }

    suspend fun process(item: ScheduledItem, since: Instant) {
        process(item, since, mostRecentlyProcessedAt(item))
    }

    suspend fun process(
        item: ScheduledItem,
        since: Instant,
        mostRecentProcessTime: Instant?,
    ) {
        findNextProcessTime(item, since, mostRecentProcessTime).let { nextProcessTime ->
            todoItemDao.insert(TodoItem(text = item.description))
            scheduledItemHistoryDao.insert(
                ScheduledItemHistory(
                    scheduledItemId = item.id,
                    processedAt = Instant.now(),
                )
            )
        }
    }

    fun findNextProcessTime(
        item: ScheduledItem,
        since: Instant,
        mostRecentProcessTime: Instant?,
    ): Instant? {
        val cutoff = mostRecentProcessTime
            ?.let { maxOf(since, it) }
            ?: since

        val nextProcessTime = item.cronExpression?.let { cron ->
            val executionTime = ExecutionTime.forCron(cron)
            val cutoffZoned = cutoff.atZone(ZoneId.systemDefault())
            executionTime.nextExecution(cutoffZoned).orElse(null)?.toInstant()
        }

        if (nextProcessTime == null) {
            return null
        }

        if (item.expiration?.isBefore(nextProcessTime) == true) {
            return null
        }

        val now = Instant.now()
        if (now.isBefore(nextProcessTime)) {
            return null
        }

        return nextProcessTime
    }

    suspend fun mostRecentlyProcessedAt(item: ScheduledItem): Instant? =
        scheduledItemHistoryDao.getMostRecentForItem(item.id).firstOrNull()?.processedAt
}