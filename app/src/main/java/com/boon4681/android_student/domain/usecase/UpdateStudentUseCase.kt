package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.domain.repository.StudentRepository
import javax.inject.Inject

class UpdateStudentUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(student: Student) {
        repository.updateStudent(student)
    }
}
