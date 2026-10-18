package com.boon4681.android_student.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.boon4681.android_student.data.remote.AuthApi
import com.boon4681.android_student.data.remote.RetrofitClient
import com.boon4681.android_student.data.remote.TokenStorage
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : ViewModel() {
    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onUsernameChange(value: String) {
        username = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = authApi.login(
                    username = username,
                    password = password
                )
                tokenStorage.saveToken(result.access_token)
                password = ""
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "เข้าสู่ระบบไม่สำเร็จ กรุณาตรวจสอบข้อมูล"
            } finally {
                isLoading = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                val tokenStorage = TokenStorage(app)
                LoginViewModel(
                    authApi = RetrofitClient.create(tokenStorage).create(AuthApi::class.java),
                    tokenStorage = tokenStorage
                )
            }
        }
    }
}
