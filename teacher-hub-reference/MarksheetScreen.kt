package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TabulationRow
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksheetScreen(viewModel: TeacherViewModel) {
    val searchQuery by viewModel.marksheetSearchQuery.collectAsStateWithLifecycle()
    val marksheet by viewModel.searchedMarksheet.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Search Marksheet",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onMarksheetSearchChanged,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Enter Student Roll Number") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            singleLine = true
        )

        if (marksheet != null) {
            MarksheetCard(marksheet!!, allSubjects)
        } else if (searchQuery.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No student found with Roll $searchQuery")
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Search for a student to view their marksheet")
            }
        }
    }
}

@Composable
fun MarksheetCard(row: TabulationRow, allSubjects: List<SubjectEntity>) {
    val scrollState = rememberScrollState()
    
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header Content
            Text("ACADEMIC TRANSCRIPT", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Name: ${row.student.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Roll No: ${row.student.rollNumber}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GPA", style = MaterialTheme.typography.labelMedium)
                    val gpaColor = if (row.finalGpa > 0 || row.finalGrade == "-") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val gpaText = if (row.finalGrade == "-") "-" else row.finalGpa.toString()
                    Text(gpaText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = gpaColor)
                    Text("Merit: ${row.meritPosition}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Grades Table
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subject", modifier = Modifier.weight(0.35f), fontWeight = FontWeight.Bold)
                        Text("Full Marks", modifier = Modifier.weight(0.25f), fontWeight = FontWeight.Bold)
                        Text("Obtained", modifier = Modifier.weight(0.25f), fontWeight = FontWeight.Bold)
                        Text("Grade", modifier = Modifier.weight(0.15f), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    
                    // Table Rows
                    allSubjects.forEach { subj ->
                        val res = row.results[subj.id]
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subj.title, modifier = Modifier.weight(0.35f), fontSize = 14.sp)
                            
                            Column(modifier = Modifier.weight(0.25f)) {
                                val maxParts = mutableListOf<String>()
                                if (subj.hasMcq) maxParts.add(subj.maxMcq.toString())
                                if (subj.hasWritten) maxParts.add(subj.maxWritten.toString())
                                if (subj.hasPractical) maxParts.add(subj.maxPractical.toString())
                                val maxComp = maxParts.joinToString(" ")
                                
                                if (maxParts.size > 1) {
                                    Text(maxComp, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(subj.maxMarks.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Text(subj.maxMarks.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Column(modifier = Modifier.weight(0.25f)) {
                                if (res != null && res.total > 0) {
                                    val evtParts = mutableListOf<String>()
                                    if (subj.hasMcq) evtParts.add(res.mcq?.toString() ?: "-")
                                    if (subj.hasWritten) evtParts.add(res.written?.toString() ?: "-")
                                    if (subj.hasPractical) evtParts.add(res.practical?.toString() ?: "-")
                                    val evtComp = evtParts.joinToString(" ")
                                    
                                    if (evtParts.size > 1) {
                                        Text(evtComp, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(res.total.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        Text(res.total.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("-", fontSize = 15.sp)
                                }
                            }
                            
                            if (res != null && res.total > 0) {
                                Text(res.grade.letter, modifier = Modifier.weight(0.15f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("-", modifier = Modifier.weight(0.15f), fontSize = 14.sp)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    
                    // Final Row
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Bold)
                        Text(row.totalMarks.toString(), modifier = Modifier.weight(0.25f), fontWeight = FontWeight.Bold)
                        Text(row.finalGrade, modifier = Modifier.weight(0.15f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
