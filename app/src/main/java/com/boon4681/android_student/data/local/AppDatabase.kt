package com.boon4681.android_student.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StudentEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
