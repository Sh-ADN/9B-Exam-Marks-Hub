package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abutorab.marks9b.data.local.entity.TermEntity
import com.abutorab.marks9b.ui.MarksViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TermListScreen(
    yearId: Int,
    viewModel: MarksViewModel,
    onNavigateToTermDetail: (Int) -> Unit
) {
    val terms by viewModel.getTermsForYear(yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var termToEdit by remember { mutableStateOf<TermEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Term")
            }
        }
    ) { innerPadding ->
        if (terms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "No terms",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No terms yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap + to add Mid Term or Annual",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                                .combinedClickable(
                                    onClick = { onNavigateToTermDetail(term.id) },
                                    onLongClick = { termToEdit = term }
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
                                    Icon(
                                        imageVector = if (term.examPeriod == "MID_TERM") Icons.Default.Edit else Icons.Default.DateRange,
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
                                        text = if (term.examPeriod == "MID_TERM") "Mid Term" else "Annual",
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

        if (showAddDialog) {
            var label by remember { mutableStateOf("") }
            var examPeriod by remember { mutableStateOf(com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name) }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Term") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Term (e.g. Half Yearly)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Exam Period", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name,
                                onClick = { examPeriod = com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name }
                            )
                            Text("Mid Term")
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.ANNUAL.name,
                                onClick = { examPeriod = com.abutorab.marks9b.data.local.entity.ExamPeriod.ANNUAL.name }
                            )
                            Text("Annual")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.insertTerm(yearId, label, examPeriod)
                            showAddDialog = false
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (termToEdit != null) {
            var label by remember { mutableStateOf(termToEdit!!.label) }
            var examPeriod by remember { mutableStateOf(termToEdit!!.examPeriod) }
            AlertDialog(
                onDismissRequest = { termToEdit = null },
                title = { Text("Edit Term") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Term") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Exam Period", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name,
                                onClick = { examPeriod = com.abutorab.marks9b.data.local.entity.ExamPeriod.MID_TERM.name }
                            )
                            Text("Mid Term")
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = examPeriod == com.abutorab.marks9b.data.local.entity.ExamPeriod.ANNUAL.name,
                                onClick = { examPeriod = com.abutorab.marks9b.data.local.entity.ExamPeriod.ANNUAL.name }
                            )
                            Text("Annual")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.updateTerm(termToEdit!!.copy(label = label, examPeriod = examPeriod))
                            termToEdit = null
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { termToEdit = null }) { Text("Cancel") }
                }
            )
        }
    }
}
