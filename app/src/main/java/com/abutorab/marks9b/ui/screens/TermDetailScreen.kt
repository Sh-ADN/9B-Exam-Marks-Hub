package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.marks9b.data.local.entity.*
import com.abutorab.marks9b.ui.MarksViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TermDetailScreen(
    termId: Int,
    viewModel: MarksViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Students", "Subjects")
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Term Details") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (selectedTabIndex == 0) {
                StudentsTab(termId = termId, viewModel = viewModel, snackbarHostState = snackbarHostState, coroutineScope = coroutineScope)
            } else {
                SubjectsTab(termId = termId, viewModel = viewModel, snackbarHostState = snackbarHostState, coroutineScope = coroutineScope)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsTab(termId: Int, viewModel: MarksViewModel, snackbarHostState: SnackbarHostState, coroutineScope: kotlinx.coroutines.CoroutineScope) {
    val students by viewModel.getStudentsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(students, key = { it.id }) { student ->
                SwipeToDeleteContainer(
                    item = student,
                    confirmTitle = "Delete student?",
                    confirmMessage = "Delete ${student.name} (Roll ${student.roll})?",
                    onDelete = { 
                        coroutineScope.launch {
                            viewModel.snapshotAndDeleteStudent(it)
                            val result = snackbarHostState.showSnackbar("Student deleted", "Undo", duration = SnackbarDuration.Short)
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoLastDelete()
                            }
                        }
                    }
                ) {
                    ListItem(
                        headlineContent = { Text(student.name) },
                        supportingContent = { Text("Roll: ${student.roll} | Rel: ${student.religion} | Grp: ${student.group}") }
                    )
                    HorizontalDivider()
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Student")
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            AddStudentForm(termId = termId, onSubmit = { student ->
                viewModel.insertStudent(student)
                showAddSheet = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsTab(termId: Int, viewModel: MarksViewModel, snackbarHostState: SnackbarHostState, coroutineScope: kotlinx.coroutines.CoroutineScope) {
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(subjects, key = { it.id }) { subject ->
                SwipeToDeleteContainer(
                    item = subject,
                    confirmTitle = "Delete subject?",
                    confirmMessage = "Delete ${subject.name}?",
                    onDelete = { 
                        coroutineScope.launch {
                            viewModel.snapshotAndDeleteSubject(it)
                            val result = snackbarHostState.showSnackbar("Subject deleted", "Undo", duration = SnackbarDuration.Short)
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoLastDelete()
                            }
                        }
                    }
                ) {
                    ListItem(
                        headlineContent = { Text(subject.name) },
                        supportingContent = { Text("Full: ${subject.fullMarks} | Pass: ${subject.passMarks} | Role: ${subject.sheetRole}") }
                    )
                    HorizontalDivider()
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Subject")
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            AddSubjectForm(termId = termId, onSubmit = { subject ->
                viewModel.insertSubject(subject)
                showAddSheet = false
            })
        }
    }
}

    // SwipeToDeleteContainer moved to its own file

@Composable
fun AddStudentForm(termId: Int, onSubmit: (StudentEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var roll by remember { mutableStateOf("") }
    var religion by remember { mutableStateOf(Religion.ISLAM) }
    var group by remember { mutableStateOf(StudentGroup.SCIENCE) }
    var optional by remember { mutableStateOf(OptionalType.HIGHER_MATH) }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Add Student", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Name") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = roll, onValueChange = { roll = it },
            label = { Text("Roll") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        
        EnumDropdown(label = "Religion", selected = religion.name, options = Religion.values().map { it.name }) {
            religion = Religion.valueOf(it)
        }
        Spacer(Modifier.height(8.dp))
        EnumDropdown(label = "Group", selected = group.name, options = StudentGroup.values().map { it.name }) {
            group = StudentGroup.valueOf(it)
        }
        Spacer(Modifier.height(8.dp))
        EnumDropdown(label = "Optional Type", selected = optional.name, options = OptionalType.values().map { it.name }) {
            optional = OptionalType.valueOf(it)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val rollInt = roll.toIntOrNull() ?: 0
                if (name.isNotBlank() && rollInt > 0) {
                    onSubmit(
                        StudentEntity(
                            termId = termId, roll = rollInt, name = name,
                            religion = religion.code, group = group.code, optionalType = optional.code
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun AddSubjectForm(termId: Int, onSubmit: (SubjectEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var fullMarks by remember { mutableStateOf("") }
    var passMarks by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(SheetRole.NONE) }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Add Subject", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Name") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fullMarks, onValueChange = { fullMarks = it },
                label = { Text("Full Marks") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = passMarks, onValueChange = { passMarks = it },
                label = { Text("Pass Marks") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(Modifier.height(8.dp))
        
        EnumDropdown(label = "Sheet Role", selected = role.name, options = SheetRole.values().map { it.name }) {
            role = SheetRole.valueOf(it)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val full = fullMarks.toIntOrNull() ?: 0
                val pass = passMarks.toIntOrNull() ?: 0
                if (name.isNotBlank() && full > 0) {
                    onSubmit(
                        SubjectEntity(
                            termId = termId, name = name, fullMarks = full,
                            passMarks = pass, sheetRole = role.code
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }
        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
