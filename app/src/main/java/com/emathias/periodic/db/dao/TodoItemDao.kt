package com.emathias.periodic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emathias.periodic.db.entities.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoItemDao {
    @Insert
    suspend fun insertAll(vararg todoItems: TodoItem)

    @Update
    suspend fun updateItems(vararg todoItems: TodoItem)

    @Query("SELECT * FROM TodoItem")
    fun getAll(): Flow<List<TodoItem>>
}