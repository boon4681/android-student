package com.boon4681.android_student.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boon4681.android_student.domain.model.Student
import com.boon4681.android_student.worker.SyncScheduler

@Composable
fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

@Composable
fun StudentScreen(
    students: List<Student>,
    query: String,
    isOnline: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onQueryChange: (String) -> Unit,
    onLoad: () -> Unit,
    onSync: () -> Unit,
    onStudentClick: (Student) -> Unit,
    onDelete: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OnlineStatus(isOnline = isOnline)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onSync() }) {
                    Text("Sync Now")
                }
                IconButton(onClick = { onLoad() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Load Students")
                }
            }
        }

        OutlinedButton(
            onClick = { SyncScheduler.syncNow(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sync in Background")
        }

        Spacer(Modifier.height(8.dp))

        TextField(
            value = query,
            onValueChange = { onQueryChange(it) },
            placeholder = { Text("Search student...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            StudentList(
                students = students,
                onStudentClick = onStudentClick,
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun OnlineStatus(isOnline: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isOnline) Color(0xFF2E7D32) else Color.Gray)
        )
        Spacer(Modifier.width(8.dp))
        Text(if (isOnline) "Online" else "Offline")
    }
}

@Composable
fun StudentList(
    students: List<Student>,
    onStudentClick: (Student) -> Unit,
    onDelete: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    if (students.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students")
        }
        return
    }
    LazyColumn(modifier = modifier) {
        items(students, key = { it.id }) { student ->
            StudentItem(
                student = student,
                onClick = { onStudentClick(student) },
                onDelete = { onDelete(student) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun StudentItem(
    student: Student,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${student.studentCode}  ${student.name}",
                    fontWeight = FontWeight.Bold
                )
                Text(student.major, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Sync: ${student.syncStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (student.syncStatus) {
                        "SYNCED" -> Color(0xFF2E7D32)
                        "FAILED" -> MaterialTheme.colorScheme.error
                        else -> Color(0xFFEF6C00)
                    }
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
