package com.expenseos.app

import android.app.Application
import com.expenseos.app.features.notifications.NotificationScheduler
import com.expenseos.app.core.db.AppDatabase
import com.expenseos.app.core.db.AppRepository

class ExpenseOsApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database) }

    override fun onCreate() {
        super.onCreate()
        
        // Schedule daily notifications
        NotificationScheduler.scheduleDailyDigest(this)
    }
}
