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
    suspend fun insert(scheduledItem: ScheduledItem): Long

    @Update
    suspend fun update(scheduledItem: ScheduledItem)

    @Query("SELECT * FROM ScheduledItem")
    fun getAll(): Flow<List<ScheduledItem>>
}