package com.boon4681.android_student.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boon4681.android_student.domain.model.SessionExpiredException
import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.domain.repository.NetworkMonitor
import com.boon4681.android_student.domain.usecase.AddStudentUseCase
import com.boon4681.android_student.domain.usecase.DeleteStudentUseCase
import com.boon4681.android_student.domain.usecase.GetStudentsUseCase
import com.boon4681.android_student.domain.usecase.RefreshStudentsUseCase
import com.boon4681.android_student.domain.usecase.SyncStudentsUseCase
import com.boon4681.android_student.domain.usecase.UpdateStudentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    getStudentsUseCase: GetStudentsUseCase,
    private val refreshStudentsUseCase: RefreshStudentsUseCase,
    private val syncStudentsUseCase: SyncStudentsUseCase,
    private val addStudentUseCase: AddStudentUseCase,
    private val updateStudentUseCase: UpdateStudentUseCase,
    private val deleteStudentUseCase: DeleteStudentUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {
    val students: StateFlow<List<Student>> =
        getStudentsUseCase()
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
        if (e is SessionExpiredException) {
            sessionExpired = true
        } else {
            errorMessage = message
        }
    }

    fun onQueryChange(value: String) {
        query = value
    }

    fun filterStudents(list: List<Student>): List<Student> {
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
                refreshStudentsUseCase()
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
                syncStudentsUseCase()
                refreshStudentsUseCase()
            } catch (e: Exception) {
                showError(e, "Sync ไม่สำเร็จ ลองใหม่เมื่อเชื่อมต่อ Server ได้")
            } finally {
                isLoading = false
            }
        }
    }

    fun addStudent(studentCode: String, name: String, major: String) {
        viewModelScope.launch {
            errorMessage = null
            try {
                addStudentUseCase(studentCode, name, major)
            } catch (e: IllegalArgumentException) {
                errorMessage = e.message
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            updateStudentUseCase(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            errorMessage = null
            try {
                deleteStudentUseCase(student)
            } catch (e: Exception) {
                showError(e, "ลบไม่สำเร็จ ต้องเชื่อมต่อ Server ก่อน")
            }
        }
    }
}
