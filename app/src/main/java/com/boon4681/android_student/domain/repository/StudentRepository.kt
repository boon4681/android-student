package com.boon4681.android_student.domain.repository

import com.boon4681.android_student.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeStudents(): Flow<List<Student>>

    suspend fun refreshStudents()

    suspend fun addStudent(student: Student)

    suspend fun updateStudent(student: Student)

    suspend fun deleteStudent(student: Student)

    suspend fun syncPending()
}
