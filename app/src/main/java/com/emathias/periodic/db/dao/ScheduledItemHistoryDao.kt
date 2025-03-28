package com.emathias.periodic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.emathias.periodic.db.entities.ScheduledItemHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
@Singleton
interface ScheduledItemHistoryDao {
    @Insert
    suspend fun insert(historyItem: ScheduledItemHistory): Long

    @Query("SELECT * FROM ScheduledItemHistory")
    fun getAll(): Flow<List<ScheduledItemHistory>>

    @Query("SELECT * FROM ScheduledItemHistory WHERE scheduledItemId = :scheduledItemId")
    fun getAllForItem(scheduledItemId: Long): Flow<List<ScheduledItemHistory>>

    @Query(
        """
            SELECT * FROM ScheduledItemHistory
            WHERE scheduledItemId = :scheduledItemId
            ORDER BY processedAt DESC
            LIMIT 1
        """
    )
    fun getMostRecentForItem(scheduledItemId: Long): Flow<ScheduledItemHistory>
}