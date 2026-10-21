package com.boon4681.android_student.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.boon4681.android_student.data.NetworkMonitor
import com.boon4681.android_student.data.local.AppDatabase
import com.boon4681.android_student.data.local.StudentEntity
import com.boon4681.android_student.data.remote.RetrofitClient
import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.TokenStorage
import com.boon4681.android_student.data.repository.StudentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel(
    private val repository: StudentRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {
    val students: StateFlow<List<StudentEntity>> =
        repository.observeStudents()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var query by mutableStateOf("")
        private set

    var sessionExpired by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline = it }
        }
    }

    fun onSessionExpiredHandled() {
        sessionExpired = false
    }

    private fun showError(e: Exception, message: String) {
        if (e is HttpException && e.code() == 401) {
            sessionExpired = true
        } else {
            errorMessage = message
        }
    }

    fun onQueryChange(value: String) {
        query = value
    }

    fun filterStudents(list: List<StudentEntity>): List<StudentEntity> {
        val q = query.trim()
        if (q.isEmpty()) return list
        return list.filter {
            it.studentCode.contains(q, ignoreCase = true) ||
                it.name.contains(q, ignoreCase = true) ||
                it.major.contains(q, ignoreCase = true)
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.refreshStudents()
            } catch (e: Exception) {
                showError(e, "เชื่อมต่อ Server ไม่ได้ แสดงข้อมูลในเครื่อง")
            } finally {
                isLoading = false
            }
        }
    }

    fun syncStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.syncPending()
                repository.refreshStudents()
            } catch (e: Exception) {
                showError(e, "Sync ไม่สำเร็จ ลองใหม่เมื่อเชื่อมต่อ Server ได้")
            } finally {
                isLoading = false
            }
        }
    }

    fun addStudent(studentCode: String, name: String, major: String) {
        viewModelScope.launch {
            repository.addStudent(
                StudentEntity(studentCode = studentCode, name = name, major = major)
            )
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            errorMessage = null
            try {
                repository.deleteStudent(student)
            } catch (e: Exception) {
                showError(e, "ลบไม่สำเร็จ ต้องเชื่อมต่อ Server ก่อน")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                StudentViewModel(
                    repository = StudentRepository(
                        dao = AppDatabase.getInstance(app).studentDao(),
                        api = RetrofitClient.create(TokenStorage(app)).create(StudentApi::class.java)
                    ),
                    networkMonitor = NetworkMonitor(app)
                )
            }
        }
    }
}
