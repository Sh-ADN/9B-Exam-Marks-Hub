package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abutorab.marks9b.data.local.entity.YearEntity
import com.abutorab.marks9b.ui.MarksViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun YearListScreen(
    viewModel: MarksViewModel,
    onNavigateToTerms: (Int) -> Unit
) {
    val years by viewModel.getAllYears().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var yearToEdit by remember { mutableStateOf<YearEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Years") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Year")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(years, key = { it.id }) { year ->
                SwipeToDeleteContainer(
                    item = year,
                    confirmTitle = "Delete year?",
                    confirmMessage = "This will permanently delete '${year.label}' and ALL its terms, students, and subjects. This cannot be undone.",
                    onDelete = { 
                        coroutineScope.launch {
                            viewModel.snapshotAndDeleteYear(it)
                            val result = snackbarHostState.showSnackbar("Year deleted", "Undo", duration = SnackbarDuration.Short)
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoLastDelete()
                            }
                        }
                    }
                ) {
                    ListItem(
                        headlineContent = { Text(year.label) },
                        modifier = Modifier.combinedClickable(
                            onClick = { onNavigateToTerms(year.id) },
                            onLongClick = { yearToEdit = year }
                        )
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showAddDialog) {
            var label by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Year") },
                text = {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Year (e.g. 2026)") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.insertYear(label)
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

        if (yearToEdit != null) {
            var label by remember { mutableStateOf(yearToEdit!!.label) }
            AlertDialog(
                onDismissRequest = { yearToEdit = null },
                title = { Text("Edit Year") },
                text = {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Year") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.updateYear(yearToEdit!!.copy(label = label))
                            yearToEdit = null
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { yearToEdit = null }) { Text("Cancel") }
                }
            )
        }
    }
}
