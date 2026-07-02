package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.abutorab.marks9b.data.local.entity.SubjectEntity
import com.abutorab.marks9b.ui.MarksViewModel

private data class ColumnSlot(val sheetRole: String, val applicabilityValue: String?, val label: String, val width: Dp)

private fun columnWidthFor(subjectsInGroup: List<SubjectEntity>): Dp {
    val maxParts = subjectsInGroup.maxOf { s -> listOfNotNull(s.mcqMax, s.writtenMax, s.practicalMax).size }
    return when {
        maxParts <= 1 -> 76.dp
        maxParts == 2 -> 112.dp
        else -> 152.dp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabulationScreen(termId: Int, viewModel: MarksViewModel, onNavigateToMarksheet: (Int) -> Unit, onBack: () -> Unit) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    val currentTerm = term ?: return
    val students by viewModel.getStudentsForYear(currentTerm.yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    val marks by viewModel.getMarksForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    val results = remember(students, subjects, marks) {
        TabulationEngine.compute(students, subjects, marks)
    }

    val columnSlots = remember(subjects) {
        subjects.filter { it.sheetRole != SheetRole.NONE.name }
            .groupBy { TabulationDisplay.columnGroupKey(it) }
            .map { (_, subjectsInGroup) ->
                val first = subjectsInGroup.first()
                ColumnSlot(
                    sheetRole = first.sheetRole,
                    applicabilityValue = first.applicabilityValue,
                    label = TabulationDisplay.bengaliLabel(first.sheetRole, first.applicabilityValue),
                    width = columnWidthFor(subjectsInGroup)
                )
            }
            .sortedBy { TabulationDisplay.canonicalOrder(it.sheetRole, it.applicabilityValue) }
    }

    val summaryColumns = remember { listOf("Total" to 64.dp, "GPA" to 56.dp, "Grade" to 56.dp, "Rank" to 48.dp) }
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
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(vertical = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    HeaderCell("Roll", 44.dp)
                    HeaderCell("Name", 120.dp, alignStart = true)
                    Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                        columnSlots.forEach { slot -> HeaderCell(slot.label, slot.width) }
                        summaryColumns.forEach { (label, width) -> HeaderCell(label, width) }
                    }
                }
                HorizontalDivider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(results, key = { it.student.id }) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToMarksheet(result.student.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DataCell(result.student.roll.toString(), 44.dp)
                            DataCell(result.student.name, 120.dp, alignStart = true)
                            Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                                columnSlots.forEach { slot ->
                                    val sr = result.subjectResults.find {
                                        it.subject.sheetRole == slot.sheetRole &&
                                            (it.subject.sheetRole != SheetRole.BGS_OR_SCIENCE.name || it.subject.applicabilityValue == slot.applicabilityValue)
                                    }
                                    val text = TabulationDisplay.formatBreakdown(sr?.mcqMarks, sr?.writtenMarks, sr?.practicalMarks, sr?.total ?: 0)
                                    val cellColor = if (sr?.letterGrade == "F") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    DataCell(text, slot.width, color = cellColor)
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
private fun HeaderCell(text: String, width: Dp, alignStart: Boolean = false) {
    Box(modifier = Modifier.width(width).padding(horizontal = 3.dp), contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center
        )
    }
}

@Composable
private fun DataCell(text: String, width: Dp, alignStart: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface, fontWeight: FontWeight = FontWeight.Normal) {
    Box(modifier = Modifier.width(width).padding(horizontal = 4.dp), contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center
        )
    }
}
