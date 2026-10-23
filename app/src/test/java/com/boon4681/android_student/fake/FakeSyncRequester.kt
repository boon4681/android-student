package com.boon4681.android_student.fake

import com.boon4681.android_student.domain.repository.SyncRequester

class FakeSyncRequester : SyncRequester {
    var requests = 0

    override fun requestSync() {
        requests++
    }
}
