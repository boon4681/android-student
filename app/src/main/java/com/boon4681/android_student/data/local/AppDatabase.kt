package com.boon4681.android_student.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boon4681.android_student.model.Student

@Database(
    entities = [Student::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val seed = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    INSERT INTO students (id, studentCode, name, major) VALUES
                    (1, '65010001', 'Somchai', 'Computer Science'),
                    (2, '65010002', 'Suda', 'Information Technology'),
                    (3, '65010003', 'Mana', 'Digital Technology')
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student.db"
                ).addCallback(seed).build().also { instance = it }
            }
    }
}
