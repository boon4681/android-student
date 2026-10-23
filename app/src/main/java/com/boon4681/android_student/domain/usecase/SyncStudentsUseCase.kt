package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.domain.repository.StudentRepository
import javax.inject.Inject

class SyncStudentsUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke() {
        repository.syncPending()
    }
}
