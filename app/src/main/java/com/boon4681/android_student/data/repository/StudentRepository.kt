package com.boon4681.android_student.data.repository

import com.boon4681.android_student.data.local.StudentDao
import com.boon4681.android_student.data.local.StudentEntity
import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.StudentCreateRequest
import android.util.Log
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi
) {
    fun observeStudents(): Flow<List<StudentEntity>> {
        return dao.observeStudents()
    }

    suspend fun refreshStudents() {
        val remoteStudents = api.getStudents()
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

    suspend fun addStudent(student: StudentEntity) {
        dao.insertStudent(
            student.copy(
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(
            student.copy(
                syncStatus = "PENDING",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteStudent(student: StudentEntity) {
        if (student.serverId != null) {
            api.deleteStudent(student.serverId)
        }
        dao.deleteStudent(student)
    }

    suspend fun syncPending() {
        val pending = dao.getPendingStudents()
        Log.d("StudentRepository", "syncPending: ${pending.size} pending student(s)")

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
                Log.d("StudentRepository", "Synced ${student.studentCode} -> serverId ${response.id}")
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    throw e
                }
                dao.updateStudent(student.copy(syncStatus = "FAILED"))
            }
        }
    }
}
