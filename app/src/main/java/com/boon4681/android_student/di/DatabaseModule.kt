package com.boon4681.android_student.di

import android.content.Context
import androidx.room.Room
import com.boon4681.android_student.data.local.AppDatabase
import com.boon4681.android_student.data.local.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "student.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideStudentDao(
        database: AppDatabase
    ): StudentDao {
        return database.studentDao()
    }
}
