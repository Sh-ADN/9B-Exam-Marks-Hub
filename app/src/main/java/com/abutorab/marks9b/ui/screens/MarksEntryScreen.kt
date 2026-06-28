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
                Text(
                    text = "Link a Google Sheet to this year first (edit the year to add one).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp)
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
                HorizontalDivider()
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

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${student.roll} - ${student.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text("Total: $total", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (subject.mcqMax != null) {
                OutlinedTextField(
                    value = mcqText,
                    onValueChange = { mcqText = it; trySave() },
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
                    onValueChange = { writtenText = it; trySave() },
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
                    onValueChange = { practicalText = it; trySave() },
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
