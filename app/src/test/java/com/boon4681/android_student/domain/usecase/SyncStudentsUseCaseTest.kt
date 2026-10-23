package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.fake.FakeStudentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncStudentsUseCaseTest {

    @Test
    fun `sync students calls repository sync once`() = runTest {
        val repository = FakeStudentRepository()
        val useCase = SyncStudentsUseCase(repository)

        useCase()

        assertEquals(1, repository.syncCalls)
    }
}
