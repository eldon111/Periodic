package com.emathias.periodic.db.dao

import com.emathias.periodic.db.entities.ScheduledItemHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.random.Random

object FakeScheduledItemHistoryDao : ScheduledItemHistoryDao {
    val map = mutableMapOf<Long, ScheduledItemHistory>()

    override suspend fun insert(historyItem: ScheduledItemHistory): Long {
        val updatedItem =
            if (historyItem.id > 0) {
                historyItem
            } else {
                historyItem.copy(id = Random.Default.nextLong())
            }

        map.put(historyItem.id, updatedItem)
        return updatedItem.id
    }

    override fun getAll(): Flow<List<ScheduledItemHistory>> {
        return flowOf(map.values.toList())
    }

    override fun getAllForItem(scheduledItemId: Long): Flow<List<ScheduledItemHistory>> {
        return getAll().map { it.filter { it.scheduledItemId == scheduledItemId } }
    }

    override fun getMostRecentForItem(scheduledItemId: Long): Flow<ScheduledItemHistory> {
        return getAllForItem(scheduledItemId).map { it.maxBy { it.processedAt } }
    }

}