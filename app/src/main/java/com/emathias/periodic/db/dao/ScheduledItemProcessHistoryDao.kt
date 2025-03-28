package com.emathias.periodic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emathias.periodic.db.entities.ScheduledItemProcessHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
@Singleton
interface ScheduledItemProcessHistoryDao {
    @Insert
    suspend fun insert(historyItem: ScheduledItemProcessHistory): Long

    @Query("SELECT * FROM ScheduledItemProcessHistory")
    fun getAll(): Flow<List<ScheduledItemProcessHistory>>

    @Query(
        """
            SELECT * FROM ScheduledItemProcessHistory
            ORDER BY processedAt DESC
            LIMIT 1
        """
    )
    fun getMostRecent(): ScheduledItemProcessHistory?
}