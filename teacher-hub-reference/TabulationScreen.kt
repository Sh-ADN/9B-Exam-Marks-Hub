package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabulationScreen(viewModel: TeacherViewModel) {
    val tabulationData by viewModel.tabulationData.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()

    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Tabulation Sheet",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (tabulationData.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No data available.")
            }
            return@Column
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(vScroll)
                    .horizontalScroll(hScroll)
            ) {
                // --- HEADER ROW (Sticky Top) ---
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Max)
                        .graphicsLayer {
                            translationY = vScroll.value.toFloat()
                        }
                        .zIndex(2f)
                ) {
                    // Frozen Left & Top
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .fillMaxHeight()
                            .graphicsLayer {
                                translationX = hScroll.value.toFloat()
                            }
                            .zIndex(3f)
                            .background(Color(0xFF64B5F6))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(12.dp),
                        contentAlignment = androidx.compose.ui.Alignment.CenterStart
                    ) {
                        Text("Roll & Name", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Rest of Header (Top sticky only)
                    allSubjects.forEach { subj ->
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF64B5F6))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .padding(12.dp),
                            contentAlignment = androidx.compose.ui.Alignment.CenterStart
                        ) {
                            Text(subj.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
                        }
                    }
                    val totals = listOf("Total Marks", "Final GPA", "Grade", "Merit")
                    totals.forEach { title ->
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF64B5F6))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .padding(12.dp),
                            contentAlignment = androidx.compose.ui.Alignment.CenterStart
                        ) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }

                // --- DATA ROWS ---
                tabulationData.forEach { rowData ->
                    Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                        // Frozen Left (Data)
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    translationX = hScroll.value.toFloat()
                                }
                                .zIndex(1f)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .padding(12.dp)
                        ) {
                            Text("${rowData.student.rollNumber} - ${rowData.student.name}")
                        }

                        // Rest of Data
                        allSubjects.forEach { subj ->
                            val result = rowData.results[subj.id]
                            val annotatedText = if (result != null && result.total > 0) {
                                buildAnnotatedString {
                                    val parts = mutableListOf<String>()
                                    if (subj.hasMcq) parts.add(result.mcq?.toString() ?: "-")
                                    if (subj.hasWritten) parts.add(result.written?.toString() ?: "-")
                                    if (subj.hasPractical) parts.add(result.practical?.toString() ?: "-")
                                    
                                    if (parts.size > 1) {
                                        withStyle(SpanStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                                            append(parts.joinToString("+"))
                                            append("=${result.total}")
                                        }
                                        withStyle(SpanStyle(fontSize = 11.sp)) {
                                            append(" | ${result.grade.letter}")
                                        }
                                    } else {
                                        withStyle(SpanStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                                            append(result.total.toString())
                                        }
                                        withStyle(SpanStyle(fontSize = 11.sp)) {
                                            append(" | ${result.grade.letter}")
                                        }
                                    }
                                }
                            } else {
                                buildAnnotatedString { append("-") }
                            }
                            
                            val isFail = result?.grade?.point == 0.0 && result.total > 0
                            
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .fillMaxHeight()
                                    .background(if (isFail) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .padding(12.dp),
                                contentAlignment = androidx.compose.ui.Alignment.CenterStart
                            ) {
                                Text(
                                    text = annotatedText, 
                                    color = if (isFail) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Totals Data
                        Box(modifier = Modifier.width(90.dp).fillMaxHeight().border(0.5.dp, MaterialTheme.colorScheme.outlineVariant).padding(12.dp), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
                            Text(rowData.totalMarks.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        val isNoMarks = rowData.finalGrade == "-"
                        val gpaColor = if (rowData.finalGpa > 0 || isNoMarks) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        val gpaText = if (isNoMarks) "-" else rowData.finalGpa.toString()
                        
                        Box(modifier = Modifier.width(90.dp).fillMaxHeight().border(0.5.dp, MaterialTheme.colorScheme.outlineVariant).padding(12.dp), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
                            Text(gpaText, color = gpaColor, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(90.dp).fillMaxHeight().border(0.5.dp, MaterialTheme.colorScheme.outlineVariant).padding(12.dp), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
                            Text(rowData.finalGrade, color = gpaColor, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(90.dp).fillMaxHeight().background(MaterialTheme.colorScheme.tertiaryContainer).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant).padding(12.dp), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
                            Text(rowData.meritPosition.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
