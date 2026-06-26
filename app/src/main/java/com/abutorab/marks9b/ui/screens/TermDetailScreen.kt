package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
    viewModel: MarksViewModel,
    onNavigateToMarksEntry: (Int, Int) -> Unit
) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    if (term == null) return
    val yearId = term!!.yearId

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Students", "Subjects", "Marks")
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(term!!.label) },
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
                StudentsTab(yearId = yearId, viewModel = viewModel, snackbarHostState = snackbarHostState, coroutineScope = coroutineScope)
            } else if (selectedTabIndex == 1) {
                SubjectsTab(termId = termId, viewModel = viewModel, snackbarHostState = snackbarHostState, coroutineScope = coroutineScope)
            } else {
                MarksTab(termId = termId, viewModel = viewModel, onNavigateToMarksEntry = onNavigateToMarksEntry)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentsTab(yearId: Int, viewModel: MarksViewModel, snackbarHostState: SnackbarHostState, coroutineScope: kotlinx.coroutines.CoroutineScope) {
    val students by viewModel.getStudentsForYear(yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddSheet by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.importStudentsFromCsv(yearId, uri, context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
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
                        supportingContent = { Text("Roll: ${student.roll} | Rel: ${student.religion} | Grp: ${student.group}") },
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { studentToEdit = student }
                        )
                    )
                    HorizontalDivider()
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalAlignment = Alignment.End) {
            SmallFloatingActionButton(onClick = { launcher.launch("*/*") }) {
                Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = "Upload CSV")
            }
            Spacer(Modifier.height(8.dp))
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            StudentForm(yearId = yearId, initialStudent = null, onSubmit = { student ->
                viewModel.insertStudent(student)
                showAddSheet = false
            })
        }
    }

    if (studentToEdit != null) {
        ModalBottomSheet(onDismissRequest = { studentToEdit = null }) {
            StudentForm(yearId = yearId, initialStudent = studentToEdit, onSubmit = { student ->
                viewModel.updateStudent(student)
                studentToEdit = null
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubjectsTab(termId: Int, viewModel: MarksViewModel, snackbarHostState: SnackbarHostState, coroutineScope: kotlinx.coroutines.CoroutineScope) {
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddSheet by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }

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
                        supportingContent = { Text("Full: ${subject.fullMarks} | Pass: ${subject.passMarks} | Role: ${subject.sheetRole}") },
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { subjectToEdit = subject }
                        )
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
            SubjectForm(termId = termId, initialSubject = null, onSubmit = { subject ->
                viewModel.insertSubject(subject)
                showAddSheet = false
            })
        }
    }

    if (subjectToEdit != null) {
        ModalBottomSheet(onDismissRequest = { subjectToEdit = null }) {
            SubjectForm(termId = termId, initialSubject = subjectToEdit, onSubmit = { subject ->
                viewModel.updateSubject(subject)
                subjectToEdit = null
            })
        }
    }
}

@Composable
fun MarksTab(termId: Int, viewModel: MarksViewModel, onNavigateToMarksEntry: (Int, Int) -> Unit) {
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp), modifier = Modifier.fillMaxSize()) {
        items(subjects, key = { it.id }) { subject ->
            ListItem(
                headlineContent = { Text(subject.name) },
                modifier = Modifier.clickable {
                    onNavigateToMarksEntry(termId, subject.id)
                }
            )
            HorizontalDivider()
        }
    }
}

// SwipeToDeleteContainer moved to its own file

@Composable
fun StudentForm(yearId: Int, initialStudent: StudentEntity?, onSubmit: (StudentEntity) -> Unit) {
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var roll by remember { mutableStateOf(initialStudent?.roll?.toString() ?: "") }
    var religion by remember { mutableStateOf(Religion.fromCode(initialStudent?.religion ?: Religion.ISLAM.code)) }
    var group by remember { mutableStateOf(StudentGroup.fromCode(initialStudent?.group ?: StudentGroup.SCIENCE.code)) }
    var optional by remember { mutableStateOf(OptionalType.fromCode(initialStudent?.optionalType ?: OptionalType.HIGHER_MATH.code)) }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text(if (initialStudent == null) "Add Student" else "Edit Student", style = MaterialTheme.typography.titleLarge)
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
            optional = if (group == StudentGroup.SCIENCE) OptionalType.HIGHER_MATH else OptionalType.AGRICULTURE
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
                            id = initialStudent?.id ?: 0,
                            yearId = yearId, roll = rollInt, name = name,
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
fun SubjectForm(termId: Int, initialSubject: SubjectEntity?, onSubmit: (SubjectEntity) -> Unit) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var fullMarks by remember { mutableStateOf(initialSubject?.fullMarks?.toString() ?: "") }
    var passMarks by remember { mutableStateOf(initialSubject?.passMarks?.toString() ?: "") }
    var role by remember { mutableStateOf(SheetRole.fromCode(initialSubject?.sheetRole ?: SheetRole.NONE.code)) }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text(if (initialSubject == null) "Add Subject" else "Edit Subject", style = MaterialTheme.typography.titleLarge)
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
                            id = initialSubject?.id ?: 0,
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
