package com.boon4681.android_student.fake

import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeStudentRepository(
    initial: List<Student> = listOf(
        Student(
            id = 1,
            studentCode = "66001",
            name = "สมชาย",
            major = "IT",
            syncStatus = "SYNCED"
        )
    )
) : StudentRepository {

    val students = MutableStateFlow(initial)
    var syncCalls = 0
    var refreshCalls = 0
    var errorToThrow: Exception? = null

    override fun observeStudents(): Flow<List<Student>> {
        return students
    }

    override suspend fun refreshStudents() {
        refreshCalls++
        errorToThrow?.let { throw it }
    }

    override suspend fun addStudent(student: Student) {
        students.value = students.value + student.copy(id = students.value.size + 1)
    }

    override suspend fun updateStudent(student: Student) {
        students.value = students.value.map { if (it.id == student.id) student else it }
    }

    override suspend fun deleteStudent(student: Student) {
        errorToThrow?.let { throw it }
        students.value = students.value.filter { it.id != student.id }
    }

    override suspend fun syncPending() {
        syncCalls++
        errorToThrow?.let { throw it }
        students.value = students.value.map { it.copy(syncStatus = "SYNCED") }
    }
}
