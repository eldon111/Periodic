package com.emathias.periodic.db.dao

import com.emathias.periodic.db.entities.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random

object FakeTodoItemDao : TodoItemDao {
    val map = mutableMapOf<Long, TodoItem>()

    override suspend fun insert(todoItem: TodoItem): Long {
        val updatedItem =
            if (todoItem.id > 0) {
                todoItem
            } else {
                todoItem.copy(id = Random.Default.nextLong())
            }

        map.put(updatedItem.id, updatedItem)
        return updatedItem.id
    }

    override suspend fun update(todoItem: TodoItem) {
        if (map.containsKey(todoItem.id)) {
            map.put(todoItem.id, todoItem)
        } else {
            throw RuntimeException("TodoItem ${todoItem.id} not found")
        }
    }

    override fun getAll(): Flow<List<TodoItem>> {
        return flowOf(map.values.toList())
    }

    override fun getOpen(): Flow<List<TodoItem>> =
        flowOf(map.values.filter { !it.completed }.sortedBy { it.dueDate })

    override fun getCompleted(): Flow<List<TodoItem>> =
        flowOf(map.values.filter { it.completed }.sortedByDescending { it.completedAt })

    override suspend fun getOpenForScheduledItem(scheduledItemId: Long): TodoItem? =
        map.values.firstOrNull { it.scheduledItemId == scheduledItemId && !it.completed }

    override suspend fun countOpen(): Int = map.values.count { !it.completed }

    override suspend fun countOverdue(today: Long): Int =
        map.values.count { !it.completed && it.dueDate.toEpochDay() < today }

}