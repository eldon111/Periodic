package com.emathias.periodic.db.dao

import com.emathias.periodic.db.entities.ScheduledItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random

object FakeScheduledItemDao : ScheduledItemDao {
    val map = mutableMapOf<Long, ScheduledItem>()

    override suspend fun insert(scheduledItem: ScheduledItem): Long {
        val updatedItem =
            if (scheduledItem.id > 0) {
                scheduledItem
            } else {
                scheduledItem.copy(id = Random.Default.nextLong())
            }

        map.put(scheduledItem.id, updatedItem)
        return updatedItem.id
    }

    override suspend fun update(scheduledItem: ScheduledItem) {
        if (map.containsKey(scheduledItem.id)) {
            map.put(scheduledItem.id, scheduledItem)
        } else {
            throw RuntimeException("ScheduledItem ${scheduledItem.id} not found")
        }
    }

    override fun getAll(): Flow<List<ScheduledItem>> {
        return flowOf(map.values.toList())
    }

}