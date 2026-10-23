package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.domain.repository.StudentRepository
import javax.inject.Inject

class AddStudentUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(studentCode: String, name: String, major: String) {
        require(studentCode.isNotBlank()) { "Student code is required" }
        require(name.isNotBlank()) { "Name is required" }
        require(major.isNotBlank()) { "Major is required" }

        repository.addStudent(
            Student(
                studentCode = studentCode.trim(),
                name = name.trim(),
                major = major.trim()
            )
        )
    }
}
