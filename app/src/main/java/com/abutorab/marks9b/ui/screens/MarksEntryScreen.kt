package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.marks9b.data.local.entity.StudentEntity
import com.abutorab.marks9b.ui.MarksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksEntryScreen(termId: Int, subjectId: Int, viewModel: MarksViewModel) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    if (term == null) return
    val yearId = term!!.yearId

    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    val subject = subjects.find { it.id == subjectId }
    val students by viewModel.getStudentsForYear(yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    val marks by viewModel.getMarksForSubject(subjectId).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject?.name ?: "Enter Marks") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(students, key = { it.id }) { student ->
                val mark = marks.find { it.studentId == student.id }
                MarkEntryRow(
                    student = student,
                    fullMarks = subject?.fullMarks ?: 100,
                    existingMark = mark?.marksObtained,
                    onSaveMark = { value ->
                        viewModel.saveMark(student.id, subjectId, value)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun MarkEntryRow(
    student: StudentEntity,
    fullMarks: Int,
    existingMark: Int?,
    onSaveMark: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(existingMark?.toString() ?: "") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(existingMark) {
        if (existingMark != null) {
            val currentParsed = textValue.toIntOrNull()
            if (currentParsed != existingMark) {
                textValue = existingMark.toString()
                isError = false
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text("${student.roll} - ${student.name}", style = MaterialTheme.typography.bodyLarge)
        }
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                if (newValue.isEmpty()) {
                    isError = false
                } else {
                    val parsed = newValue.toIntOrNull()
                    if (parsed != null && parsed in 0..fullMarks) {
                        isError = false
                        onSaveMark(parsed)
                    } else {
                        isError = true
                    }
                }
            },
            modifier = Modifier.width(120.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError,
            label = { Text("/ $fullMarks") },
            singleLine = true
        )
    }
}
