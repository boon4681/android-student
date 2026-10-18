package com.boon4681.android_student.data.repository

import com.boon4681.android_student.data.local.StudentDao
import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.StudentCreateRequest
import com.boon4681.android_student.data.remote.StudentDto
import com.boon4681.android_student.model.Student

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi
) {
    suspend fun getStudents(): List<Student> {
        return try {
            val remoteStudents = api.getStudents()
            val localStudents = remoteStudents.map { toStudent(it) }
            dao.deleteAll()
            dao.insertAll(localStudents)
            localStudents
        } catch (e: Exception) {
            dao.getAllStudents()
        }
    }

    suspend fun getLocalStudents(): List<Student> {
        return dao.getAllStudents()
    }

    suspend fun addStudent(student: Student) {
        val created = api.createStudent(toRequest(student))
        dao.insertStudent(toStudent(created))
    }

    suspend fun updateStudent(student: Student) {
        val updated = api.updateStudent(student.id, toRequest(student))
        dao.updateStudent(toStudent(updated))
    }

    suspend fun deleteStudent(student: Student) {
        api.deleteStudent(student.id)
        dao.deleteStudent(student)
    }

    private fun toStudent(dto: StudentDto): Student {
        return Student(
            id = dto.id,
            studentCode = dto.student_code,
            name = dto.name,
            major = dto.major
        )
    }

    private fun toRequest(student: Student): StudentCreateRequest {
        return StudentCreateRequest(
            student_code = student.studentCode,
            name = student.name,
            major = student.major
        )
    }
}
