package com.expenseos.app.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expenseos.app.core.db.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: String)
    
    @Query("UPDATE budgets SET spentAmount = :spentAmount WHERE id = :id")
    suspend fun updateSpentAmount(id: String, spentAmount: Long)
    
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    suspend fun getBudgetsForMonthSync(monthYear: String): List<BudgetEntity>
}
