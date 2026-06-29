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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.abutorab.marks9b.data.local.PreferencesHelper
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
    val context = LocalContext.current
    LaunchedEffect(termId) {
        PreferencesHelper.saveLastOpenedTerm(context, termId)
    }

    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    if (term == null) return
    val yearId = term!!.yearId
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.importStudentsFromCsv(yearId, uri, context)
        }
    }

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
                    ),
                    actions = {
                        if (selectedTabIndex == 0) {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Import CSV") },
                                    onClick = {
                                        showMenu = false
                                        launcher.launch("*/*")
                                    }
                                )
                            }
                        }
                    }
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
                SubjectsTab(termId = termId, viewModel = viewModel)
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (students.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "No students",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No students yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Import a CSV or tap + to add one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { studentToEdit = student }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(1).uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)) {
                                            Text(text = "Roll ${student.roll}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)) {
                                            Text(text = student.group, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                        if (student.religion.isNotBlank()) {
                                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)) {
                                                Text(text = student.religion, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectsTab(termId: Int, viewModel: MarksViewModel) {
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    val compulsory = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.ALL.name }
    val religion = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.RELIGION.name }
    val groupElectives = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.GROUP.name }
    val optional = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.OPTIONAL_TYPE.name }

    Box(modifier = Modifier.fillMaxSize()) {
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "No subjects",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No subjects yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Subjects are added automatically when you create a term",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                if (compulsory.isNotEmpty()) {
                    item { SubjectSectionHeader("Compulsory") }
                    items(compulsory, key = { it.id }) { subject -> SubjectListRow(subject) }
                }
                if (religion.isNotEmpty()) {
                    item { SubjectSectionHeader("Religion") }
                    items(religion, key = { it.id }) { subject -> SubjectListRow(subject) }
                }
                if (groupElectives.isNotEmpty()) {
                    item { SubjectSectionHeader("Group Electives") }
                    items(groupElectives, key = { it.id }) { subject -> SubjectListRow(subject) }
                }
                if (optional.isNotEmpty()) {
                    item { SubjectSectionHeader("Optional") }
                    items(optional, key = { it.id }) { subject -> SubjectListRow(subject) }
                }
            }
        }
    }
}

@Composable
fun SubjectSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SubjectListRow(subject: com.abutorab.marks9b.data.local.entity.SubjectEntity, onClick: (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (subject.applicabilityType) {
                    com.abutorab.marks9b.data.local.entity.ApplicabilityType.ALL.name -> Icons.Default.CheckCircle
                    com.abutorab.marks9b.data.local.entity.ApplicabilityType.GROUP.name -> Icons.Default.List
                    com.abutorab.marks9b.data.local.entity.ApplicabilityType.OPTIONAL_TYPE.name -> Icons.Default.AddCircle
                    com.abutorab.marks9b.data.local.entity.ApplicabilityType.RELIGION.name -> Icons.Default.Star
                    else -> Icons.Default.DateRange
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Full Marks: ${subject.fullMarks}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go to marks",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MarksTab(termId: Int, viewModel: MarksViewModel, onNavigateToMarksEntry: (Int, Int) -> Unit) {
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    val compulsory = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.ALL.name }
    val religion = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.RELIGION.name }
    val groupElectives = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.GROUP.name }
    val optional = subjects.filter { it.applicabilityType == com.abutorab.marks9b.data.local.entity.ApplicabilityType.OPTIONAL_TYPE.name }

    Box(modifier = Modifier.fillMaxSize()) {
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "No subjects",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No subjects yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Subjects are added automatically when you create a term",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                if (compulsory.isNotEmpty()) {
                    item { SubjectSectionHeader("Compulsory") }
                    items(compulsory, key = { it.id }) { subject -> 
                        SubjectListRow(subject, onClick = { onNavigateToMarksEntry(termId, subject.id) }) 
                    }
                }
                if (religion.isNotEmpty()) {
                    item { SubjectSectionHeader("Religion") }
                    items(religion, key = { it.id }) { subject -> 
                        SubjectListRow(subject, onClick = { onNavigateToMarksEntry(termId, subject.id) }) 
                    }
                }
                if (groupElectives.isNotEmpty()) {
                    item { SubjectSectionHeader("Group Electives") }
                    items(groupElectives, key = { it.id }) { subject -> 
                        SubjectListRow(subject, onClick = { onNavigateToMarksEntry(termId, subject.id) }) 
                    }
                }
                if (optional.isNotEmpty()) {
                    item { SubjectSectionHeader("Optional") }
                    items(optional, key = { it.id }) { subject -> 
                        SubjectListRow(subject, onClick = { onNavigateToMarksEntry(termId, subject.id) }) 
                    }
                }
            }
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
