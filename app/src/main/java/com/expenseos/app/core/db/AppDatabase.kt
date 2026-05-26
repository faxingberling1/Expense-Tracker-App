package com.expenseos.app.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expenseos.app.core.db.entities.BudgetEntity
import com.expenseos.app.core.db.entities.RewardEventEntity
import com.expenseos.app.core.db.entities.TransactionEntity
import com.expenseos.app.core.db.entities.UdhaarEntity
import com.expenseos.app.core.db.entities.SavingsGoalEntity

@Database(
    entities = [
        TransactionEntity::class,
        UdhaarEntity::class,
        BudgetEntity::class,
        RewardEventEntity::class,
        SavingsGoalEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun udhaarDao(): UdhaarDao
    abstract fun budgetDao(): BudgetDao
    abstract fun rewardDao(): RewardDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expenseos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
