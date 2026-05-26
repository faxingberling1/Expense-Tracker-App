package com.expenseos.app.features.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

object NotificationScheduler {
    private const val DAILY_DIGEST_WORK_NAME = "daily_digest_work"

    fun scheduleDailyDigest(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyDigestWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelayTo8PM(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_DIGEST_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    private fun calculateInitialDelayTo8PM(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (now.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
