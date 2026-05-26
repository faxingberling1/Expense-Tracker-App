package com.expenseos.app.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expenseos.app.core.db.entities.RewardEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: RewardEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<RewardEventEntity>)

    @Query("SELECT * FROM reward_events ORDER BY timestamp DESC")
    fun observeAllEvents(): Flow<List<RewardEventEntity>>

    @Query("SELECT SUM(points) FROM reward_events")
    fun observeTotalPoints(): Flow<Int?>
}
