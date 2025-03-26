package com.emathias.periodic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emathias.periodic.db.entities.ScheduledItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledItemDao {
    @Insert
    suspend fun insertAll(vararg todoItems: ScheduledItem)

    @Update
    suspend fun updateItems(vararg todoItems: ScheduledItem)

    @Query("SELECT * FROM ScheduledItem")
    fun getAll(): Flow<List<ScheduledItem>>
}