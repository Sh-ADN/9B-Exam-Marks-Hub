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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abutorab.marks9b.data.local.entity.StudentEntity
import com.abutorab.marks9b.data.local.entity.SubjectEntity
import com.abutorab.marks9b.data.local.entity.MarkEntity
import com.abutorab.marks9b.data.local.entity.ApplicabilityType
import com.abutorab.marks9b.ui.MarksViewModel

import com.abutorab.marks9b.data.remote.SheetsSyncService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksEntryScreen(termId: Int, subjectId: Int, viewModel: MarksViewModel) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    val currentTerm = term ?: return
    val yearId = currentTerm.yearId

    val year by viewModel.getYearById(yearId).collectAsStateWithLifecycle(initialValue = null)
    val currentYear = year ?: return

    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    val subject = subjects.find { it.id == subjectId } ?: return
    val allStudents by viewModel.getStudentsForYear(yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    val marks by viewModel.getMarksForSubject(subjectId).collectAsStateWithLifecycle(initialValue = emptyList())

    val filteredStudents = remember(allStudents, subject) {
        allStudents.filter { student ->
            when (subject.applicabilityType) {
                ApplicabilityType.ALL.name -> true
                ApplicabilityType.RELIGION.name -> student.religion == subject.applicabilityValue
                ApplicabilityType.GROUP.name -> student.group == subject.applicabilityValue
                ApplicabilityType.OPTIONAL_TYPE.name -> student.optionalType == subject.applicabilityValue
                else -> true
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var hasAutoImported by remember(subjectId) { mutableStateOf(false) }

    fun performImport(showFeedback: Boolean) {
        val sheetId = currentYear.sheetId ?: return
        isImporting = true
        coroutineScope.launch {
            val componentCount = listOf(subject.mcqMax, subject.writtenMax, subject.practicalMax).count { it != null }
            val startColumn = if (currentTerm.examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name) {
                3
            } else {
                3 + componentCount
            }
            val result = SheetsSyncService.importSubjectMarks(sheetId, subject.sheetTabName, startColumn, componentCount)
            isImporting = false
            if (result.isSuccess) {
                var updatedCount = 0
                result.getOrNull()?.forEach { entry ->
                    val student = filteredStudents.find { it.roll == entry.roll }
                    if (student != null) {
                        val existingMark = marks.find { it.studentId == student.id }
                        var newMcq = existingMark?.mcqMarks
                        var newWritten = existingMark?.writtenMarks
                        var newPractical = existingMark?.practicalMarks
                        var changed = false
                        var idx = 0
                        if (subject.mcqMax != null) {
                            entry.values.getOrNull(idx)?.let { newMcq = it; changed = true }
                            idx++
                        }
                        if (subject.writtenMax != null) {
                            entry.values.getOrNull(idx)?.let { newWritten = it; changed = true }
                            idx++
                        }
                        if (subject.practicalMax != null) {
                            entry.values.getOrNull(idx)?.let { newPractical = it; changed = true }
                            idx++
                        }
                        if (changed) {
                            viewModel.saveMark(
                                MarkEntity(
                                    id = existingMark?.id ?: 0,
                                    studentId = student.id,
                                    subjectId = subjectId,
                                    mcqMarks = newMcq,
                                    writtenMarks = newWritten,
                                    practicalMarks = newPractical
                                )
                            )
                            updatedCount++
                        }
                    }
                }
                if (showFeedback) {
                    snackbarHostState.showSnackbar("Imported $updatedCount marks")
                }
            } else if (showFeedback) {
                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Import failed")
            }
        }
    }

    LaunchedEffect(subjectId, allStudents) {
        if (!hasAutoImported && allStudents.isNotEmpty()) {
            hasAutoImported = true
            performImport(showFeedback = false)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(subject.name) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    val sheetId = currentYear.sheetId
                    if (isImporting) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(
                            onClick = { performImport(showFeedback = true) },
                            enabled = sheetId != null
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Import from Sheet")
                        }
                    }
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp))
                    } else {
                        Button(
                            onClick = {
                                if (sheetId != null) {
                                    isExporting = true
                                    coroutineScope.launch {
                                        val entries = filteredStudents.mapNotNull { student ->
                                            val mark = marks.find { it.studentId == student.id }
                                            if (mark == null) null
                                            else {
                                                val values = mutableListOf<Int>()
                                                if (subject.mcqMax != null) values.add(mark.mcqMarks ?: 0)
                                                if (subject.writtenMax != null) values.add(mark.writtenMarks ?: 0)
                                                if (subject.practicalMax != null) values.add(mark.practicalMarks ?: 0)
                                                SheetsSyncService.ExportEntry(student.roll, student.name, values)
                                            }
                                        }
                                        
                                        var componentCount = 0
                                        if (subject.mcqMax != null) componentCount++
                                        if (subject.writtenMax != null) componentCount++
                                        if (subject.practicalMax != null) componentCount++
                                        
                                        val startColumn = if (currentTerm.examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name) {
                                            3
                                        } else {
                                            3 + componentCount
                                        }
                                        
                                        val result = SheetsSyncService.exportSubjectMarks(sheetId, subject.sheetTabName, startColumn, entries)
                                        isExporting = false
                                        if (result.isSuccess) {
                                            snackbarHostState.showSnackbar("Exported ${result.getOrNull()} students successfully")
                                        } else {
                                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Export failed")
                                        }
                                    }
                                }
                            },
                            enabled = sheetId != null,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Export to Sheet")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (currentYear.sheetId == null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                ) {
                    Text(
                        text = "Link a Google Sheet to this year first (edit the year to add one).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            text = "No students match this subject's applicability rules",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val mark = marks.find { it.studentId == student.id }
                        MarkEntryRow(
                            student = student,
                            subject = subject,
                            existingMark = mark,
                            onSaveMark = { mcq, written, practical ->
                                viewModel.saveMark(
                                    MarkEntity(
                                        id = mark?.id ?: 0,
                                        studentId = student.id,
                                        subjectId = subjectId,
                                        mcqMarks = mcq,
                                        writtenMarks = written,
                                        practicalMarks = practical
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarkEntryRow(
    student: StudentEntity,
    subject: SubjectEntity,
    existingMark: MarkEntity?,
    onSaveMark: (mcq: Int?, written: Int?, practical: Int?) -> Unit
) {
    var mcqText by remember(existingMark?.mcqMarks) { mutableStateOf(existingMark?.mcqMarks?.toString() ?: "") }
    var writtenText by remember(existingMark?.writtenMarks) { mutableStateOf(existingMark?.writtenMarks?.toString() ?: "") }
    var practicalText by remember(existingMark?.practicalMarks) { mutableStateOf(existingMark?.practicalMarks?.toString() ?: "") }

    var mcqError by remember { mutableStateOf(false) }
    var writtenError by remember { mutableStateOf(false) }
    var practicalError by remember { mutableStateOf(false) }

    fun trySave() {
        val mcqParsed = mcqText.toIntOrNull()
        val writtenParsed = writtenText.toIntOrNull()
        val practicalParsed = practicalText.toIntOrNull()

        mcqError = mcqText.isNotEmpty() && (mcqParsed == null || subject.mcqMax == null || mcqParsed !in 0..subject.mcqMax)
        writtenError = writtenText.isNotEmpty() && (writtenParsed == null || subject.writtenMax == null || writtenParsed !in 0..subject.writtenMax)
        practicalError = practicalText.isNotEmpty() && (practicalParsed == null || subject.practicalMax == null || practicalParsed !in 0..subject.practicalMax)

        if (!mcqError && !writtenError && !practicalError) {
            val finalMcq = if (mcqText.isEmpty()) existingMark?.mcqMarks else mcqParsed
            val finalWritten = if (writtenText.isEmpty()) existingMark?.writtenMarks else writtenParsed
            val finalPractical = if (practicalText.isEmpty()) existingMark?.practicalMarks else practicalParsed
            
            // Only save if something actually changed from what's stored to avoid infinite loops,
            // or we could just always save if valid since room upserts. Let's just always save if valid.
            onSaveMark(finalMcq, finalWritten, finalPractical)
        }
    }

    val total = (mcqText.toIntOrNull() ?: 0) + (writtenText.toIntOrNull() ?: 0) + (practicalText.toIntOrNull() ?: 0)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("${student.roll} - ${student.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text("Total: $total", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (subject.mcqMax != null) {
                OutlinedTextField(
                    value = mcqText,
                    onValueChange = { newValue ->
                        val parsed = newValue.toIntOrNull()
                        if (newValue.isEmpty() || (parsed != null && subject.mcqMax != null && parsed in 0..subject.mcqMax)) {
                            mcqText = newValue
                            trySave()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mcqError,
                    label = { Text("MCQ / ${subject.mcqMax}") },
                    singleLine = true
                )
            }
            if (subject.writtenMax != null) {
                OutlinedTextField(
                    value = writtenText,
                    onValueChange = { newValue ->
                        val parsed = newValue.toIntOrNull()
                        if (newValue.isEmpty() || (parsed != null && subject.writtenMax != null && parsed in 0..subject.writtenMax)) {
                            writtenText = newValue
                            trySave()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = writtenError,
                    label = { Text("CQ / ${subject.writtenMax}") },
                    singleLine = true
                )
            }
            if (subject.practicalMax != null) {
                OutlinedTextField(
                    value = practicalText,
                    onValueChange = { newValue ->
                        val parsed = newValue.toIntOrNull()
                        if (newValue.isEmpty() || (parsed != null && subject.practicalMax != null && parsed in 0..subject.practicalMax)) {
                            practicalText = newValue
                            trySave()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = practicalError,
                    label = { Text("Prac / ${subject.practicalMax}") },
                    singleLine = true
                )
            }
        }
    }
    }
}
