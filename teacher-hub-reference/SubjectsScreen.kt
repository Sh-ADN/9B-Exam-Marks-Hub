package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(viewModel: TeacherViewModel) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Manage Subjects",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(allSubjects, key = { it.id }) { subject ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = "Subject",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "${subject.title} [ID: ${subject.id}]",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row {
                                    IconButton(onClick = { editingSubject = subject }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.deleteSubject(subject) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                            Text("Total Max: ${subject.maxMarks} | Pass: ${subject.passMarks}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "MCQ: ${if (subject.hasMcq) subject.maxMcq else "No"} | " +
                                       "Written: ${if (subject.hasWritten) subject.maxWritten else "No"} | " +
                                       "Practical: ${if (subject.hasPractical) subject.maxPractical else "No"}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingSubject != null) {
        val st = editingSubject
        var id by remember { mutableStateOf(st?.id ?: "") }
        var title by remember { mutableStateOf(st?.title ?: "") }
        var maxMarks by remember { mutableStateOf(st?.maxMarks?.toString() ?: "100") }
        var passMarks by remember { mutableStateOf(st?.passMarks?.toString() ?: "33") }
        
        var hasMcq by remember { mutableStateOf(st?.hasMcq ?: true) }
        var maxMcq by remember { mutableStateOf(st?.maxMcq?.toString() ?: "30") }
        
        var hasWritten by remember { mutableStateOf(st?.hasWritten ?: true) }
        var maxWritten by remember { mutableStateOf(st?.maxWritten?.toString() ?: "70") }
        
        var hasPractical by remember { mutableStateOf(st?.hasPractical ?: false) }
        var maxPractical by remember { mutableStateOf(st?.maxPractical?.toString() ?: "0") }

        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                editingSubject = null
            },
            title = { Text(if (st == null) "Add Subject" else "Edit Subject") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Subject ID (e.g., ENG1)") },
                        singleLine = true,
                        enabled = st == null // ID cannot be edited
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (e.g., English 1)") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Total Max Marks") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = passMarks, onValueChange = { passMarks = it }, label = { Text("Pass Marks") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Components", style = MaterialTheme.typography.titleSmall)

                    // MCQ
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = hasMcq, onCheckedChange = { hasMcq = it })
                        Text("MCQ")
                        if (hasMcq) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = maxMcq, onValueChange = { maxMcq = it }, label = { Text("Max") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().weight(1f), singleLine = true)
                        }
                    }

                    // Written
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = hasWritten, onCheckedChange = { hasWritten = it })
                        Text("Written")
                        if (hasWritten) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = maxWritten, onValueChange = { maxWritten = it }, label = { Text("Max") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().weight(1f), singleLine = true)
                        }
                    }

                    // Practical
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = hasPractical, onCheckedChange = { hasPractical = it })
                        Text("Practical")
                        if (hasPractical) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = maxPractical, onValueChange = { maxPractical = it }, label = { Text("Max") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().weight(1f), singleLine = true)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsedMax = maxMarks.toIntOrNull() ?: 100
                        val parsedPass = passMarks.toIntOrNull() ?: 33
                        val pmcq = maxMcq.toIntOrNull() ?: 0
                        val pwritten = maxWritten.toIntOrNull() ?: 0
                        val ppractical = maxPractical.toIntOrNull() ?: 0
                        
                        if (id.isNotBlank() && title.isNotBlank()) {
                            val newSubject = SubjectEntity(id, title, parsedMax, parsedPass, hasMcq, pmcq, hasWritten, pwritten, hasPractical, ppractical)
                            if (st == null) {
                                viewModel.addSubject(id, title, parsedMax, parsedPass, hasMcq, pmcq, hasWritten, pwritten, hasPractical, ppractical)
                            } else {
                                viewModel.updateSubject(newSubject)
                            }
                            showAddDialog = false
                            editingSubject = null
                        }
                    }
                ) {
                    Text(if (st == null) "Add" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    editingSubject = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
