package com.boon4681.android_student.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boon4681.android_student.StudentApplication
import java.io.IOException

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork started (attempt ${runAttemptCount + 1})")

        return try {
            val app = applicationContext as StudentApplication
            app.appContainer
                .studentRepository
                .syncPending()

            Log.d(TAG, "Sync success")
            Result.success()
        } catch (e: IOException) {
            Log.w(TAG, "Network error, retry later: ${e.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        const val TAG = "SyncWorker"
    }
}
