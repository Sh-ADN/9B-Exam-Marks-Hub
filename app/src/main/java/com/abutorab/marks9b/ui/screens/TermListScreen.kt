package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abutorab.marks9b.data.local.entity.TermEntity
import com.abutorab.marks9b.data.local.entity.examPeriodLabel
import com.abutorab.marks9b.ui.MarksViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TermListScreen(
    yearId: Int,
    viewModel: MarksViewModel,
    onNavigateToTermDetail: (Int) -> Unit,
    onNavigateToCombined: () -> Unit
) {
    val terms by viewModel.getTermsForYear(yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasMidTerm = terms.any { it.examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name }
    val hasAnnual = terms.any { it.examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.ANNUAL.name }
    val canCombine = hasMidTerm && hasAnnual

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Terms") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (!hasMidTerm || !hasAnnual) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Term")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Card(
                onClick = { if (canCombine) onNavigateToCombined() },
                colors = CardDefaults.cardColors(
                    containerColor = if (canCombine) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "সমন্বিত",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canCombine) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (canCombine) "View combined Mid Term + Annual results" else "Add both অর্ধবার্ষিক and বার্ষিক to unlock",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (canCombine) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (terms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "No terms",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No terms yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Tap + to add অর্ধবার্ষিক or বার্ষিক",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(terms, key = { it.id }) { term ->
                        SwipeToDeleteContainer(
                            item = term,
                            confirmTitle = "Delete term?",
                            confirmMessage = "This will permanently delete '${term.label}' and ALL its students and subjects. This cannot be undone.",
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.snapshotAndDeleteTerm(it)
                                    val result = snackbarHostState.showSnackbar("Term deleted", "Undo", duration = SnackbarDuration.Short)
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
                                    .clickable { onNavigateToTermDetail(term.id) }
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
                                        Icon(
                                            imageVector = if (term.examPeriod == "MID_TERM") Icons.Default.Place else Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = term.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = examPeriodLabel(term.examPeriod),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Term") },
                text = {
                    Column {
                        Text(
                            "Choose which exam period to add for this year.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        TextButton(
                            onClick = {
                                viewModel.insertTerm(yearId, "অর্ধবার্ষিক", com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name)
                                showAddDialog = false
                            },
                            enabled = !hasMidTerm,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (hasMidTerm) "অর্ধবার্ষিক (already added)" else "অর্ধবার্ষিক")
                        }
                        TextButton(
                            onClick = {
                                viewModel.insertTerm(yearId, "বার্ষিক", com.abutorab.marks9b.data.local.entity.ExamPeriod.ANNUAL.name)
                                showAddDialog = false
                            },
                            enabled = !hasAnnual,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (hasAnnual) "বার্ষিক (already added)" else "বার্ষিক")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
