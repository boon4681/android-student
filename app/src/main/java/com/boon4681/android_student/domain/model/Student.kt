package com.boon4681.android_student.domain.model

data class Student(
    val id: Int = 0,
    val serverId: Int? = null,
    val studentCode: String,
    val name: String,
    val major: String,
    val syncStatus: String = "PENDING"
)
