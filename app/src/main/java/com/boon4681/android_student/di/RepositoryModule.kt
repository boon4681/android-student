package com.boon4681.android_student.di

import com.boon4681.android_student.data.AndroidNetworkMonitor
import com.boon4681.android_student.data.repository.StudentRepositoryImpl
import com.boon4681.android_student.domain.repository.NetworkMonitor
import com.boon4681.android_student.domain.repository.StudentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        implementation: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        implementation: AndroidNetworkMonitor
    ): NetworkMonitor
}
