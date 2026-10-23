package com.boon4681.android_student.data.repository

import com.boon4681.android_student.data.local.StudentEntity
import com.boon4681.android_student.data.remote.StudentDto
import com.boon4681.android_student.domain.model.SessionExpiredException
import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.fake.FakeStudentApi
import com.boon4681.android_student.fake.FakeStudentDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class StudentRepositoryImplTest {

    private val dao = FakeStudentDao()
    private val api = FakeStudentApi()
    private val repository = StudentRepositoryImpl(dao, api)

    private fun httpError(code: Int): HttpException {
        return HttpException(Response.error<Any>(code, ResponseBody.create(null, "")))
    }

    @Test
    fun `observeStudents maps entity to domain model`() = runTest {
        dao.insertStudent(
            StudentEntity(
                serverId = 7,
                studentCode = "66006001",
                name = "Lab6 Hilt",
                major = "Computer Science",
                syncStatus = "SYNCED"
            )
        )

        val student = repository.observeStudents().first().single()

        assertEquals(
            Student(
                id = 1,
                serverId = 7,
                studentCode = "66006001",
                name = "Lab6 Hilt",
                major = "Computer Science",
                syncStatus = "SYNCED"
            ),
            student
        )
    }

    @Test
    fun `addStudent saves to Room as pending without calling API`() = runTest {
        repository.addStudent(Student(studentCode = "66007001", name = "Lab7", major = "CS"))

        assertEquals("PENDING", dao.rows.value.single().syncStatus)
        assertTrue(api.createdRequests.isEmpty())
    }

    @Test
    fun `syncPending sends pending student and marks it synced`() = runTest {
        repository.addStudent(Student(studentCode = "66007001", name = "Lab7", major = "CS"))

        repository.syncPending()

        val row = dao.rows.value.single()
        assertEquals("SYNCED", row.syncStatus)
        assertEquals(100, row.serverId)
        assertEquals("66007001", api.createdRequests.single().student_code)
    }

    @Test
    fun `syncPending marks student failed when server rejects it`() = runTest {
        repository.addStudent(Student(studentCode = "66007001", name = "Lab7", major = "CS"))
        api.errorToThrow = httpError(422)

        repository.syncPending()

        assertEquals("FAILED", dao.rows.value.single().syncStatus)
    }

    @Test
    fun `syncPending keeps pending and throws when offline`() = runTest {
        repository.addStudent(Student(studentCode = "66007001", name = "Lab7", major = "CS"))
        api.errorToThrow = IOException("offline")

        val error = runCatching { repository.syncPending() }.exceptionOrNull()

        assertTrue(error is IOException)
        assertEquals("PENDING", dao.rows.value.single().syncStatus)
    }

    @Test
    fun `401 from server becomes SessionExpiredException`() = runTest {
        api.errorToThrow = httpError(401)

        val error = runCatching { repository.refreshStudents() }.exceptionOrNull()

        assertTrue(error is SessionExpiredException)
    }

    @Test
    fun `refreshStudents replaces synced rows but keeps pending rows`() = runTest {
        repository.addStudent(Student(studentCode = "66007001", name = "Offline", major = "CS"))
        api.serverStudents += StudentDto(1, "65001", "Somchai Jaidee", "Computer Science")

        repository.refreshStudents()

        val rows = dao.rows.value
        assertEquals(2, rows.size)
        assertEquals("PENDING", rows.first { it.studentCode == "66007001" }.syncStatus)
        assertEquals("SYNCED", rows.first { it.studentCode == "65001" }.syncStatus)
    }
}
