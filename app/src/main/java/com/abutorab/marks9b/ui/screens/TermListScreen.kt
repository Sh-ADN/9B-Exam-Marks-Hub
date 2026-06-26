package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abutorab.marks9b.data.local.entity.TermEntity
import com.abutorab.marks9b.ui.MarksViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermListScreen(
    yearId: Int,
    viewModel: MarksViewModel,
    onNavigateToTermDetail: (Int) -> Unit
) {
    val terms by viewModel.getTermsForYear(yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
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
                    ListItem(
                        headlineContent = { Text(term.label) },
                        modifier = Modifier.clickable { onNavigateToTermDetail(term.id) }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showAddDialog) {
            var label by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Term") },
                text = {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Term (e.g. Half Yearly)") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.insertTerm(yearId, label)
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
    }
}
