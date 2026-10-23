package com.boon4681.android_student.domain.usecase

import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentsUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(): Flow<List<Student>> {
        return repository.observeStudents()
    }
}
