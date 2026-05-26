package com.expenseos.app.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expenseos.app.core.db.entities.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun observeAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoal(id: String)
    
    @Query("UPDATE savings_goals SET savedAmount = savedAmount + :amount WHERE id = :id")
    suspend fun addFundsToGoal(id: String, amount: Long)
    
    @Query("UPDATE savings_goals SET isCompleted = 1 WHERE id = :id")
    suspend fun markGoalCompleted(id: String)
}
