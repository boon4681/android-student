package com.boon4681.android_student.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name")
    fun observeStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun getPendingStudents(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSynced()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Transaction
    suspend fun replaceSynced(students: List<StudentEntity>) {
        deleteSynced()
        insertAll(students)
    }
}
