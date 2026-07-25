package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abutorab.marks9b.data.local.entity.MarkEntity
import com.abutorab.marks9b.data.local.entity.StudentEntity
import com.abutorab.marks9b.data.local.entity.SubjectEntity
import com.abutorab.marks9b.data.local.entity.TermEntity
import com.abutorab.marks9b.data.local.entity.YearEntity
import com.abutorab.marks9b.data.local.entity.examPeriodLabel

private const val SCHOOL_NAME = "আবুতোরাব বহুমুখী উচ্চ বিদ্যালয়"
private const val ROWS_PER_COLUMN = 50
private val ROLL_COL_WIDTH = 52.dp
private val MARKS_COL_WIDTH = 168.dp

/**
 * Full-screen look-alike of the paper "নম্বর ফর্দ" (marks ledger) teachers write
 * marks on by hand, so a teacher can flip back and forth between the physical
 * sheet and this view to catch data-entry mistakes. Opened as a Dialog (not a
 * nav route) so it's a quick glance-and-dismiss rather than a deep navigation.
 */
@Composable
fun VerificationSheetDialog(
    year: YearEntity,
    term: TermEntity,
    subject: SubjectEntity,
    students: List<StudentEntity>,
    marks: List<MarkEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            VerificationSheetContent(year, term, subject, students, marks, onDismiss)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerificationSheetContent(
    year: YearEntity,
    term: TermEntity,
    subject: SubjectEntity,
    students: List<StudentEntity>,
    marks: List<MarkEntity>,
    onDismiss: () -> Unit
) {
    // Bengali by default since that's how the physical sheet itself is printed.
    var useBengali by remember { mutableStateOf(true) }

    val rolls = remember(students) { students.sortedBy { it.roll } }
    val marksByStudent = remember(marks) { marks.associateBy { it.studentId } }
    val enteredCount = remember(rolls, marksByStudent) {
        rolls.count { s ->
            marksByStudent[s.id]?.let { it.mcqMarks != null || it.writtenMarks != null || it.practicalMarks != null } == true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Marks", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = { NumeralToggle(useBengali) { useBengali = it } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            SheetHeader(year, term, subject, useBengali)
            Text(
                text = "${NumeralFormat.localize(enteredCount.toString(), useBengali)}/${NumeralFormat.localize(rolls.size.toString(), useBengali)} entered",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 12.dp)
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth().horizontalScroll(rememberScrollState())) {
                LedgerGrid(rolls, marksByStudent, useBengali)
            }
        }
    }
}

@Composable
private fun NumeralToggle(useBengali: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
        Text(
            "বাং",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (useBengali) FontWeight.Bold else FontWeight.Normal,
            color = if (useBengali) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
        Switch(checked = !useBengali, onCheckedChange = { onChange(!it) }, modifier = Modifier.padding(horizontal = 6.dp))
        Text(
            "EN",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (!useBengali) FontWeight.Bold else FontWeight.Normal,
            color = if (!useBengali) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
    }
}

private fun paperLabel(subjectName: String): String = when {
    subjectName.contains("1st Paper") -> "১ম পত্র"
    subjectName.contains("2nd Paper") -> "২য় পত্র"
    else -> "-"
}

@Composable
private fun SheetHeader(year: YearEntity, term: TermEntity, subject: SubjectEntity, useBengali: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(SCHOOL_NAME, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("পরীক্ষা: ${examPeriodLabel(term.examPeriod)}, ${year.label}", style = MaterialTheme.typography.bodySmall)
            Text("শ্রেণি: নবম, শাখা: খ", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("বিষয়: ${TabulationDisplay.bengaliSubjectName(subject)}", style = MaterialTheme.typography.bodySmall)
            Text("পত্র: ${paperLabel(subject.name)}", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                "নম্বর ফর্দ  (পূর্ণমান: ${NumeralFormat.localize(subject.fullMarks.toString(), useBengali)})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LedgerGrid(students: List<StudentEntity>, marksByStudent: Map<Int, MarkEntity>, useBengali: Boolean) {
    val columns = if (students.isEmpty()) emptyList() else students.chunked(ROWS_PER_COLUMN)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        columns.forEach { columnStudents ->
            LedgerColumn(columnStudents, marksByStudent, useBengali)
        }
    }
}

@Composable
private fun LedgerColumn(students: List<StudentEntity>, marksByStudent: Map<Int, MarkEntity>, useBengali: Boolean) {
    val outline = MaterialTheme.colorScheme.outline
    Column(modifier = Modifier.border(1.dp, outline)) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LedgerCell("রোল", ROLL_COL_WIDTH, header = true)
            VDivider(outline)
            LedgerCell("নৈঃ+রঃ+ব্যবঃ = মোট", MARKS_COL_WIDTH, header = true)
        }
        HorizontalDivider(color = outline)
        students.forEachIndexed { idx, student ->
            val mark = marksByStudent[student.id]
            val total = (mark?.mcqMarks ?: 0) + (mark?.writtenMarks ?: 0) + (mark?.practicalMarks ?: 0)
            val rawBreakdown = TabulationDisplay.formatBreakdown(mark?.mcqMarks, mark?.writtenMarks, mark?.practicalMarks, total)
            val breakdown = if (rawBreakdown == "-") "" else rawBreakdown

            Row(modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                LedgerCell(NumeralFormat.localize(student.roll.toString(), useBengali), ROLL_COL_WIDTH)
                VDivider(outline)
                LedgerCell(NumeralFormat.localize(breakdown, useBengali), MARKS_COL_WIDTH, alignStart = true)
            }
            if (idx < students.lastIndex) HorizontalDivider(color = outline.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun VDivider(color: Color) {
    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(color))
}

@Composable
private fun LedgerCell(text: String, width: Dp, header: Boolean = false, alignStart: Boolean = false) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = text,
            style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
