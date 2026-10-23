package com.boon4681.android_student.worker

import android.content.Context
import com.boon4681.android_student.domain.repository.SyncRequester
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkManagerSyncRequester @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncRequester {
    override fun requestSync() {
        SyncScheduler.syncNow(context)
    }
}
