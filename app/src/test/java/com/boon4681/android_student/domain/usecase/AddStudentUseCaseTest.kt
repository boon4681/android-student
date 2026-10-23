package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.fake.FakeStudentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddStudentUseCaseTest {

    @Test
    fun `add student trims input and saves as pending`() = runTest {
        val repository = FakeStudentRepository(initial = emptyList())
        val useCase = AddStudentUseCase(repository)

        useCase("  66007001 ", " Lab7 Test ", " Computer Science ")

        val saved = repository.students.value.single()
        assertEquals("66007001", saved.studentCode)
        assertEquals("Lab7 Test", saved.name)
        assertEquals("Computer Science", saved.major)
        assertEquals("PENDING", saved.syncStatus)
    }

    @Test
    fun `add student rejects blank name`() = runTest {
        val repository = FakeStudentRepository(initial = emptyList())
        val useCase = AddStudentUseCase(repository)

        val error = runCatching { useCase("66007001", "   ", "CS") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals(0, repository.students.value.size)
    }
}
