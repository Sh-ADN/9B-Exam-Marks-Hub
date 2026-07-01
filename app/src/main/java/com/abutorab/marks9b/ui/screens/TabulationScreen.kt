package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.marks9b.data.local.entity.SheetRole
import com.abutorab.marks9b.ui.MarksViewModel

private data class ColumnSlot(val sheetRole: String, val label: String)

private fun slotOrder(role: String): Int = when (role) {
    SheetRole.BANGLA1.name -> 0
    SheetRole.BANGLA2.name -> 1
    SheetRole.ENG1.name -> 2
    SheetRole.ENG2.name -> 3
    SheetRole.MATH.name -> 4
    SheetRole.RELIGION.name -> 5
    SheetRole.BGS_OR_SCIENCE.name -> 6
    SheetRole.ELECTIVE1.name -> 7
    SheetRole.ELECTIVE2.name -> 8
    SheetRole.ELECTIVE3.name -> 9
    SheetRole.OPTIONAL.name -> 10
    SheetRole.ICT.name -> 11
    else -> 99
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabulationScreen(termId: Int, viewModel: MarksViewModel, onBack: () -> Unit) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    val currentTerm = term ?: return
    val students by viewModel.getStudentsForYear(currentTerm.yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    val marks by viewModel.getMarksForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    val results = remember(students, subjects, marks) {
        TabulationEngine.compute(students, subjects, marks)
    }

    val columnSlots = remember(subjects) {
        subjects.groupBy { it.sheetRole }
            .filterKeys { it != SheetRole.NONE.name }
            .toList()
            .sortedBy { slotOrder(it.first) }
            .map { (role, subjectsInRole) -> ColumnSlot(role, subjectsInRole.joinToString(" / ") { it.name }) }
    }

    val horizontalScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabulation — ${currentTerm.label}", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "No data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No students yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Add students to this year to see the tabulation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCell("Roll", 44.dp)
                    HeaderCell("Name", 120.dp)
                    Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                        columnSlots.forEach { HeaderCell(it.label, 96.dp) }
                        HeaderCell("Total", 64.dp)
                        HeaderCell("GPA", 56.dp)
                        HeaderCell("Grade", 56.dp)
                        HeaderCell("Rank", 48.dp)
                    }
                }
                HorizontalDivider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(results, key = { it.student.id }) { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DataCell(result.student.roll.toString(), 44.dp)
                            DataCell(result.student.name, 120.dp, alignStart = true)
                            Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                                columnSlots.forEach { slot ->
                                    val sr = result.subjectResults.find { it.subject.sheetRole == slot.sheetRole }
                                    val text = if (sr == null || sr.total == 0) "-" else "${sr.total}"
                                    val color = if (sr?.letterGrade == "F") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    DataCell(text, 96.dp, color = color)
                                }
                                DataCell(result.grandTotal.toString(), 64.dp, fontWeight = FontWeight.Bold)
                                DataCell(result.gpa?.let { "%.2f".format(it) } ?: "-", 56.dp, color = MaterialTheme.colorScheme.tertiary)
                                DataCell(
                                    result.letterGrade.ifEmpty { "-" },
                                    56.dp,
                                    color = if (result.letterGrade == "F") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                DataCell(result.position?.toString() ?: "-", 48.dp)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Box(modifier = Modifier.width(width).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DataCell(text: String, width: Dp, alignStart: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface, fontWeight: FontWeight = FontWeight.Normal) {
    Box(modifier = Modifier.width(width).padding(horizontal = 4.dp), contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = fontWeight, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (alignStart) TextAlign.Start else TextAlign.Center)
    }
}
