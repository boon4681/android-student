package com.boon4681.android_student.fake

import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.StudentCreateRequest
import com.boon4681.android_student.data.remote.StudentDto

class FakeStudentApi : StudentApi {

    val serverStudents = mutableListOf<StudentDto>()
    val createdRequests = mutableListOf<StudentCreateRequest>()
    var errorToThrow: Exception? = null
    private var nextId = 100

    override suspend fun getStudents(): List<StudentDto> {
        errorToThrow?.let { throw it }
        return serverStudents.toList()
    }

    override suspend fun createStudent(student: StudentCreateRequest): StudentDto {
        errorToThrow?.let { throw it }
        createdRequests += student
        val dto = StudentDto(nextId++, student.student_code, student.name, student.major)
        serverStudents += dto
        return dto
    }

    override suspend fun updateStudent(id: Int, student: StudentCreateRequest): StudentDto {
        errorToThrow?.let { throw it }
        val dto = StudentDto(id, student.student_code, student.name, student.major)
        serverStudents.replaceAll { if (it.id == id) dto else it }
        return dto
    }

    override suspend fun deleteStudent(id: Int) {
        errorToThrow?.let { throw it }
        serverStudents.removeAll { it.id == id }
    }
}
