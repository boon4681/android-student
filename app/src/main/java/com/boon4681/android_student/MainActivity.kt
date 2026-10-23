package com.boon4681.android_student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.boon4681.android_student.data.remote.TokenStorage
import com.boon4681.android_student.ui.StudentApp
import com.boon4681.android_student.ui.login.LoginScreen
import com.boon4681.android_student.ui.theme.AndroidstudentTheme
import com.boon4681.android_student.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SyncScheduler.schedulePeriodicSync(this)
        setContent {
            AndroidstudentTheme {
                var isLoggedIn by remember { mutableStateOf(tokenStorage.getToken() != null) }

                if (isLoggedIn) {
                    StudentApp(
                        onLogout = {
                            tokenStorage.clearToken()
                            isLoggedIn = false
                        }
                    )
                } else {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
                }
            }
        }
    }
}
