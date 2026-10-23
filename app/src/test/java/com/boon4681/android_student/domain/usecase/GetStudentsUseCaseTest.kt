package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.fake.FakeStudentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetStudentsUseCaseTest {

    @Test
    fun `get students returns student list`() = runTest {
        val repository = FakeStudentRepository()
        val useCase = GetStudentsUseCase(repository)

        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals("สมชาย", result.first().name)
    }

    @Test
    fun `get students emits new list after repository changes`() = runTest {
        val repository = FakeStudentRepository()
        val useCase = GetStudentsUseCase(repository)

        AddStudentUseCase(repository)("66002", "สมหญิง", "CS")
        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("สมหญิง", result.last().name)
    }
}
