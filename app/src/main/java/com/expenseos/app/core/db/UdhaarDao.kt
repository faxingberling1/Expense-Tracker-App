package com.expenseos.app.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expenseos.app.core.db.entities.UdhaarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UdhaarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UdhaarEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<UdhaarEntity>)

    @Query("SELECT * FROM udhaar_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UdhaarEntity>>

    @Query("SELECT * FROM udhaar_entries WHERE isSettled = 0 ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<UdhaarEntity>>

    @Query("UPDATE udhaar_entries SET isSettled = 1, settledAt = :settledAt WHERE id = :id")
    suspend fun markSettled(id: String, settledAt: Long)
}
