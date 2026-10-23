package com.boon4681.android_student.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boon4681.android_student.domain.usecase.SyncStudentsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncStudentsUseCase: SyncStudentsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork started (attempt ${runAttemptCount + 1}), SyncStudentsUseCase injected by Hilt")

        return try {
            syncStudentsUseCase()

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
