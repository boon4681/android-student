package com.boon4681.android_student.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    fun syncNow(context: Context) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "student-sync-now",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )

        Log.d(SyncWorker.TAG, "One-time sync enqueued: ${request.id}")
    }

    fun schedulePeriodicSync(context: Context) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "student-sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )

        Log.d(SyncWorker.TAG, "Periodic sync scheduled every 15 minutes")
    }
}
