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
import com.boon4681.android_student.data.remote.RetrofitClient
import com.boon4681.android_student.data.remote.StudentApi
import com.boon4681.android_student.data.remote.TokenStorage
import com.boon4681.android_student.data.repository.StudentRepository
import com.boon4681.android_student.model.Student
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: StudentRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {
    var students by mutableStateOf(listOf<Student>())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var query by mutableStateOf("")
        private set

    val filteredStudents: List<Student>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return students
            return students.filter {
                it.studentCode.contains(q, ignoreCase = true) ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.major.contains(q, ignoreCase = true)
            }
        }

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline = it }
        }
        loadStudents()
    }

    fun onQueryChange(value: String) {
        query = value
    }

    fun findStudent(id: Int): Student? = students.find { it.id == id }

    fun loadStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                students = repository.getStudents()
            } catch (e: Exception) {
                errorMessage = "ไม่สามารถโหลดข้อมูลได้"
            } finally {
                isLoading = false
            }
        }
    }

    fun addStudent(studentCode: String, name: String, major: String) {
        runAndReload {
            repository.addStudent(
                Student(studentCode = studentCode, name = name, major = major)
            )
        }
    }

    fun updateStudent(student: Student) {
        runAndReload { repository.updateStudent(student) }
    }

    fun deleteStudent(student: Student) {
        runAndReload { repository.deleteStudent(student) }
    }

    private fun runAndReload(action: suspend () -> Unit) {
        viewModelScope.launch {
            errorMessage = null
            try {
                action()
                students = repository.getLocalStudents()
            } catch (e: Exception) {
                errorMessage = "ไม่สามารถบันทึกข้อมูลได้"
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
