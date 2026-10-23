package com.boon4681.android_student

import android.content.Context
import com.boon4681.android_student.data.local.AppDatabase
import com.boon4681.android_student.data.remote.RetrofitClient
import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.TokenStorage
import com.boon4681.android_student.data.repository.StudentRepository

class AppContainer(context: Context) {
    private val database =
        AppDatabase.getInstance(context)

    private val tokenStorage =
        TokenStorage(context)

    private val retrofit =
        RetrofitClient.create(tokenStorage)

    private val studentApi =
        retrofit.create(StudentApi::class.java)

    val studentRepository =
        StudentRepository(
            dao = database.studentDao(),
            api = studentApi
        )
}
