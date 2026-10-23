package com.boon4681.android_student.data.repository

import android.util.Log
import com.boon4681.android_student.data.local.StudentDao
import com.boon4681.android_student.data.local.StudentEntity
import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.StudentCreateRequest
import com.boon4681.android_student.domain.model.SessionExpiredException
import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val dao: StudentDao,
    private val api: StudentApi
) : StudentRepository {

    override fun observeStudents(): Flow<List<Student>> {
        return dao.observeStudents()
            .map { entities ->
                entities.map { entity -> toDomain(entity) }
            }
    }

    override suspend fun refreshStudents() {
        val remoteStudents = try {
            api.getStudents()
        } catch (e: HttpException) {
            throw toDomainError(e)
        }
        val pendingServerIds = dao.getPendingStudents().mapNotNull { it.serverId }

        val syncedStudents = remoteStudents
            .filter { it.id !in pendingServerIds }
            .map {
                StudentEntity(
                    serverId = it.id,
                    studentCode = it.student_code,
                    name = it.name,
                    major = it.major,
                    syncStatus = "SYNCED"
                )
            }

        dao.replaceSynced(syncedStudents)
    }

    override suspend fun addStudent(student: Student) {
        dao.insertStudent(
            StudentEntity(
                studentCode = student.studentCode,
                name = student.name,
                major = student.major,
                syncStatus = "PENDING"
            )
        )
    }

    override suspend fun updateStudent(student: Student) {
        dao.updateStudent(
            toEntity(student).copy(
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteStudent(student: Student) {
        if (student.serverId != null) {
            try {
                api.deleteStudent(student.serverId)
            } catch (e: HttpException) {
                throw toDomainError(e)
            }
        }
        dao.deleteStudent(toEntity(student))
    }

    override suspend fun syncPending() {
        val pending = dao.getPendingStudents()
        Log.d(TAG, "syncPending: ${pending.size} pending student(s)")

        for (student in pending) {
            val request = StudentCreateRequest(
                student_code = student.studentCode,
                name = student.name,
                major = student.major
            )

            try {
                val response = if (student.serverId == null) {
                    api.createStudent(request)
                } else {
                    api.updateStudent(student.serverId, request)
                }

                dao.updateStudent(
                    student.copy(
                        serverId = response.id,
                        syncStatus = "SYNCED"
                    )
                )
                Log.d(TAG, "Synced ${student.studentCode} -> serverId ${response.id}")
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    throw SessionExpiredException()
                }
                dao.updateStudent(student.copy(syncStatus = "FAILED"))
            }
        }
    }

    private fun toDomain(entity: StudentEntity): Student {
        return Student(
            id = entity.localId,
            serverId = entity.serverId,
            studentCode = entity.studentCode,
            name = entity.name,
            major = entity.major,
            syncStatus = entity.syncStatus
        )
    }

    private fun toEntity(student: Student): StudentEntity {
        return StudentEntity(
            localId = student.id,
            serverId = student.serverId,
            studentCode = student.studentCode,
            name = student.name,
            major = student.major,
            syncStatus = student.syncStatus
        )
    }

    private fun toDomainError(e: HttpException): Exception {
        return if (e.code() == 401) SessionExpiredException() else e
    }

    companion object {
        private const val TAG = "StudentRepository"
    }
}
