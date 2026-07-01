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
import com.abutorab.marks9b.ui.MarksViewModel

private data class SubColumn(val type: String, val width: Dp)
private data class ColumnSlot(val sheetRole: String, val label: String, val subColumns: List<SubColumn>)

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
        subjects.groupBy { it.sheetRole }
            .filterKeys { it != SheetRole.NONE.name }
            .toList()
            .sortedBy { slotOrder(it.first) }
            .map { (role, subjectsInRole) ->
                val subCols = mutableListOf<SubColumn>()
                if (subjectsInRole.any { it.mcqMax != null }) subCols.add(SubColumn("MCQ", 40.dp))
                if (subjectsInRole.any { it.writtenMax != null }) subCols.add(SubColumn("CQ", 40.dp))
                if (subjectsInRole.any { it.practicalMax != null }) subCols.add(SubColumn("Prac", 40.dp))
                subCols.add(SubColumn("Total", 48.dp))
                ColumnSlot(role, subjectsInRole.joinToString(" / ") { it.name }, subCols)
            }
    }

    val summaryColumns = remember { listOf(SubColumn("Total", 64.dp), SubColumn("GPA", 56.dp), SubColumn("Grade", 56.dp), SubColumn("Rank", 48.dp)) }
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
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    HeaderBlock(topLabel = null, width = 44.dp) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Roll", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HeaderBlock(topLabel = null, width = 120.dp) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                            Text("Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                        columnSlots.forEach { slot ->
                            val slotWidth = slot.subColumns.fold(0.dp) { acc, sc -> acc + sc.width }
                            HeaderBlock(topLabel = slot.label, width = slotWidth) {
                                Row(Modifier.fillMaxSize()) {
                                    slot.subColumns.forEach { sc ->
                                        Box(Modifier.width(sc.width).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                            Text(sc.type, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }
                        summaryColumns.forEach { sc ->
                            HeaderBlock(topLabel = null, width = sc.width) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(sc.type, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(results, key = { it.student.id }) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToMarksheet(result.student.id) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DataCell(result.student.roll.toString(), 44.dp)
                            DataCell(result.student.name, 120.dp, alignStart = true)
                            Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                                columnSlots.forEach { slot ->
                                    val sr = result.subjectResults.find { it.subject.sheetRole == slot.sheetRole }
                                    val cellColor = if (sr?.letterGrade == "F") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    slot.subColumns.forEach { sc ->
                                        val text = when (sc.type) {
                                            "MCQ" -> sr?.mcqMarks?.toString() ?: "-"
                                            "CQ" -> sr?.writtenMarks?.toString() ?: "-"
                                            "Prac" -> sr?.practicalMarks?.toString() ?: "-"
                                            else -> if (sr == null || sr.total == 0) "-" else "${sr.total}"
                                        }
                                        val weight = if (sc.type == "Total") FontWeight.Bold else FontWeight.Normal
                                        DataCell(text, sc.width, color = cellColor, fontWeight = weight)
                                    }
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
private fun HeaderBlock(topLabel: String?, width: Dp, bottomContent: @Composable () -> Unit) {
    Column(modifier = Modifier.width(width)) {
        Box(modifier = Modifier.fillMaxWidth().height(24.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
            if (topLabel != null) {
                Text(
                    topLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            bottomContent()
        }
    }
}

@Composable
private fun DataCell(text: String, width: Dp, alignStart: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface, fontWeight: FontWeight = FontWeight.Normal) {
    Box(modifier = Modifier.width(width).padding(horizontal = 4.dp), contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = fontWeight, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (alignStart) TextAlign.Start else TextAlign.Center)
    }
}
