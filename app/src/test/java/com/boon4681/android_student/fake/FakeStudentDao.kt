package com.boon4681.android_student.fake

import com.boon4681.android_student.data.local.StudentDao
import com.boon4681.android_student.data.local.StudentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStudentDao : StudentDao {

    val rows = MutableStateFlow<List<StudentEntity>>(emptyList())
    private var nextId = 1

    override fun observeStudents(): Flow<List<StudentEntity>> {
        return rows.map { list -> list.sortedBy { it.name } }
    }

    override suspend fun getPendingStudents(): List<StudentEntity> {
        return rows.value.filter { it.syncStatus == "PENDING" || it.syncStatus == "FAILED" }
    }

    override suspend fun insertStudent(student: StudentEntity) {
        val row = if (student.localId == 0) student.copy(localId = nextId++) else student
        rows.value = rows.value.filter { it.localId != row.localId } + row
    }

    override suspend fun updateStudent(student: StudentEntity) {
        rows.value = rows.value.map { if (it.localId == student.localId) student else it }
    }

    override suspend fun deleteStudent(student: StudentEntity) {
        rows.value = rows.value.filter { it.localId != student.localId }
    }

    override suspend fun deleteSynced() {
        rows.value = rows.value.filter { it.syncStatus != "SYNCED" }
    }

    override suspend fun insertAll(students: List<StudentEntity>) {
        students.forEach { insertStudent(it) }
    }

    override suspend fun replaceSynced(students: List<StudentEntity>) {
        deleteSynced()
        insertAll(students)
    }
}
