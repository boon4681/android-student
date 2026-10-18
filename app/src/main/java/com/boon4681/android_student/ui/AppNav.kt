package com.boon4681.android_student.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.boon4681.android_student.viewmodel.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApp(
    onLogout: () -> Unit,
    viewModel: StudentViewModel = viewModel(factory = StudentViewModel.Factory)
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val title = when (currentRoute) {
        "add" -> "Add Student"
        "detail/{id}" -> "Student Detail"
        "edit/{id}" -> "Edit Student"
        else -> "Student Management"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (currentRoute != null && currentRoute != "list") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute == "list") {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentRoute == "list") {
                FloatingActionButton(onClick = { navController.navigate("add") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("list") {
                StudentScreen(
                    students = viewModel.filteredStudents,
                    query = viewModel.query,
                    isOnline = viewModel.isOnline,
                    isLoading = viewModel.isLoading,
                    errorMessage = viewModel.errorMessage,
                    onQueryChange = { viewModel.onQueryChange(it) },
                    onLoad = { viewModel.loadStudents() },
                    onStudentClick = { navController.navigate("detail/${it.id}") },
                    onDelete = { viewModel.deleteStudent(it) }
                )
            }
            composable("add") {
                StudentFormScreen(
                    student = null,
                    onSave = { code, name, major ->
                        viewModel.addStudent(code, name, major)
                        navController.popBackStack()
                    }
                )
            }
            composable(
                "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                val student = viewModel.findStudent(id)
                StudentDetailScreen(
                    student = student,
                    onEdit = { navController.navigate("edit/$id") },
                    onDelete = {
                        student?.let { viewModel.deleteStudent(it) }
                        navController.popBackStack()
                    }
                )
            }
            composable(
                "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                val student = viewModel.findStudent(id)
                StudentFormScreen(
                    student = student,
                    onSave = { code, name, major ->
                        student?.let {
                            viewModel.updateStudent(
                                it.copy(studentCode = code, name = name, major = major)
                            )
                        }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
