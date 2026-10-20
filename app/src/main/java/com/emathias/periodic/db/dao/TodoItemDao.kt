package com.emathias.periodic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emathias.periodic.db.entities.TodoItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
@Singleton
interface TodoItemDao {
    @Insert
    suspend fun insert(todoItem: TodoItem): Long

    @Update
    suspend fun update(todoItem: TodoItem)

    @Query("SELECT * FROM TodoItem")
    fun getAll(): Flow<List<TodoItem>>

    @Query("SELECT * FROM TodoItem WHERE completedAt IS NULL ORDER BY dueDate ASC, id ASC")
    fun getOpen(): Flow<List<TodoItem>>

    @Query("SELECT * FROM TodoItem WHERE completedAt IS NOT NULL ORDER BY completedAt DESC")
    fun getCompleted(): Flow<List<TodoItem>>

    @Query("SELECT * FROM TodoItem WHERE scheduledItemId = :scheduledItemId AND completedAt IS NULL LIMIT 1")
    suspend fun getOpenForScheduledItem(scheduledItemId: Long): TodoItem?

    @Query("SELECT COUNT(*) FROM TodoItem WHERE completedAt IS NULL")
    suspend fun countOpen(): Int

    @Query("SELECT COUNT(*) FROM TodoItem WHERE completedAt IS NULL AND dueDate < :today")
    suspend fun countOverdue(today: Long): Int
}
