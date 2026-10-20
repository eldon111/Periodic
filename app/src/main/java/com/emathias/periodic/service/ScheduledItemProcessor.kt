package com.emathias.periodic.service

import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.db.dao.ScheduledItemHistoryDao
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.ScheduledItemHistory
import com.emathias.periodic.db.entities.TodoItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemProcessor @Inject constructor(
    val scheduledItemDao: ScheduledItemDao,
    val scheduledItemHistoryDao: ScheduledItemHistoryDao,
    val todoItemDao: TodoItemDao,
) {

    suspend fun processAll(since: Instant) {
        scheduledItemDao.getAll().first().forEach { item ->
            println("Processing scheduled item '${item.description}'")
            process(item, since)
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
        if (item.archivedAt != null) return

        // One open to-do per scheduled item: nothing new until the user completes it.
        if (todoItemDao.getOpenForScheduledItem(item.id) != null) return

        val occurrence = findLatestProcessTime(item, since, mostRecentProcessTime) ?: return
        val now = Instant.now()
        todoItemDao.insert(
            TodoItem(
                scheduledItemId = item.id,
                title = item.title,
                description = item.description,
                dueDate = occurrence.atZone(ZoneId.systemDefault()).toLocalDate(),
                createdAt = now,
            )
        )
        scheduledItemHistoryDao.insert(
            ScheduledItemHistory(
                scheduledItemId = item.id,
                processedAt = occurrence,
            )
        )
    }

    /** The most recent occurrence that is due (not in the future) and after the cutoff. */
    fun findLatestProcessTime(
        item: ScheduledItem,
        since: Instant,
        mostRecentProcessTime: Instant?,
    ): Instant? {
        var latest: Instant? = null
        var cutoff = mostRecentProcessTime?.let { maxOf(since, it) } ?: since
        while (true) {
            val next = findNextProcessTime(item, cutoff, null) ?: return latest
            latest = next
            cutoff = next
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

        val nextProcessTime = item.schedule.nextOccurrence(cutoff) ?: return null

        val now = Instant.now()
        if (now.isBefore(nextProcessTime)) {
            return null
        }

        return nextProcessTime
    }

    suspend fun mostRecentlyProcessedAt(item: ScheduledItem): Instant? =
        scheduledItemHistoryDao.getMostRecentForItem(item.id).firstOrNull()?.processedAt
}