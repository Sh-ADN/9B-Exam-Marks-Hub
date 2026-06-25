package com.abutorab.teacher.hub.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditScreen(viewModel: TeacherViewModel) {
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val data by viewModel.activeQuickEditData.collectAsStateWithLifecycle()

    val animatedSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.surfaceVariant, animationSpec = tween(300))
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))
    val animatedOnSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurface, animationSpec = tween(300))
    val animatedSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.surface, animationSpec = tween(300))
    val animatedPrimary by animateColorAsState(targetValue = MaterialTheme.colorScheme.primary, animationSpec = tween(300))

    Column(modifier = Modifier.fillMaxSize().background(animatedSurfaceVariant)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Quick Edit Marks",
            style = MaterialTheme.typography.headlineMedium,
            color = animatedOnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Subject Selector
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedSubject?.title ?: "Select Subject",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                label = { Text("Select Subject", color = animatedOnSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = animatedOnSurface,
                    unfocusedTextColor = animatedOnSurface,
                    focusedContainerColor = animatedSurface,
                    unfocusedContainerColor = animatedSurface,
                    focusedBorderColor = animatedPrimary,
                    unfocusedBorderColor = animatedOnSurfaceVariant
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allSubjects.forEach { subj ->
                    DropdownMenuItem(
                        text = { Text(subj.title, color = animatedOnSurface) },
                        onClick = {
                            viewModel.selectSubject(subj.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Students List
        if (selectedSubject != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data, key = { it.student.rollNumber }) { item ->
                    StudentMarkRow(
                        item = item,
                        subject = selectedSubject!!,
                        onMarkChanged = { mcq, written, pract ->
                            viewModel.saveMark(item.student.rollNumber, mcq, written, pract)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StudentMarkRow(
    item: com.abutorab.teacher.hub.domain.StudentWithMark,
    subject: SubjectEntity,
    onMarkChanged: (Int?, Int?, Int?) -> Unit
) {
    var flashTrigger by remember { mutableStateOf(0) }
    var isInitialLoad by remember { mutableStateOf(true) }
    
    LaunchedEffect(item.mark) {
        if (!isInitialLoad) {
            flashTrigger++
        }
        isInitialLoad = false
    }

    val defaultColor = MaterialTheme.colorScheme.surface
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    var isHighlight by remember { mutableStateOf(false) }

    LaunchedEffect(flashTrigger) {
        if (flashTrigger > 0) {
            isHighlight = true
            kotlinx.coroutines.delay(700)
            isHighlight = false
        }
    }

    val targetColor = if (isHighlight) highlightColor else defaultColor
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedPrimaryContainer by animateColorAsState(targetValue = MaterialTheme.colorScheme.primaryContainer, animationSpec = tween(300))
    val animatedOnPrimaryContainer by animateColorAsState(targetValue = MaterialTheme.colorScheme.onPrimaryContainer, animationSpec = tween(300))
    val animatedOnSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurface, animationSpec = tween(300))
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))
    val animatedPrimary by animateColorAsState(targetValue = MaterialTheme.colorScheme.primary, animationSpec = tween(300))

    val cardRequester = remember { BringIntoViewRequester() }
    var cardSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Card(
        modifier = Modifier.fillMaxWidth()
            .onGloballyPositioned { coordinates -> cardSize = androidx.compose.ui.geometry.Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat()) }
            .bringIntoViewRequester(cardRequester),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(animatedPrimaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = animatedOnPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Roll: ${item.student.rollNumber} - ${item.student.name}", 
                    style = MaterialTheme.typography.titleMedium,
                    color = animatedOnSurface
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var mcqText by remember(item.mark?.mcq) { mutableStateOf(item.mark?.mcq?.toString() ?: "") }
                var writtenText by remember(item.mark?.written) { mutableStateOf(item.mark?.written?.toString() ?: "") }
                var practicalText by remember(item.mark?.practical) { mutableStateOf(item.mark?.practical?.toString() ?: "") }

                var mcqFocused by remember { mutableStateOf(false) }
                var writtenFocused by remember { mutableStateOf(false) }
                var practicalFocused by remember { mutableStateOf(false) }

                LaunchedEffect(mcqFocused, writtenFocused, practicalFocused) {
                    if (mcqFocused || writtenFocused || practicalFocused) {
                        cardRequester.bringIntoView()
                    }
                }

                if (subject.hasMcq) {
                    OutlinedTextField(
                        value = mcqText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxMcq)) {
                                mcqText = newVal
                                onMarkChanged(newVal.toIntOrNull(), item.mark?.written, item.mark?.practical)
                            }
                        },
                        label = { Text("MCQ (Max ${subject.maxMcq})", color = animatedOnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f).onFocusEvent { mcqFocused = it.isFocused },
                        singleLine = true,
                        isError = (mcqText.toIntOrNull() ?: 0) > subject.maxMcq,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = animatedOnSurface,
                            unfocusedTextColor = animatedOnSurface,
                            focusedBorderColor = animatedPrimary,
                            unfocusedBorderColor = animatedOnSurfaceVariant,
                            focusedLabelColor = animatedPrimary,
                            unfocusedLabelColor = animatedOnSurfaceVariant
                        )
                    )
                }
                
                if (subject.hasWritten) {
                    OutlinedTextField(
                        value = writtenText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxWritten)) {
                                writtenText = newVal
                                onMarkChanged(item.mark?.mcq, newVal.toIntOrNull(), item.mark?.practical)
                            }
                        },
                        label = { Text("Written (Max ${subject.maxWritten})", color = animatedOnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f).onFocusEvent { writtenFocused = it.isFocused },
                        singleLine = true,
                        isError = (writtenText.toIntOrNull() ?: 0) > subject.maxWritten,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = animatedOnSurface,
                            unfocusedTextColor = animatedOnSurface,
                            focusedBorderColor = animatedPrimary,
                            unfocusedBorderColor = animatedOnSurfaceVariant,
                            focusedLabelColor = animatedPrimary,
                            unfocusedLabelColor = animatedOnSurfaceVariant
                        )
                    )
                }
                
                if (subject.hasPractical) {
                    OutlinedTextField(
                        value = practicalText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxPractical)) {
                                practicalText = newVal
                                onMarkChanged(item.mark?.mcq, item.mark?.written, newVal.toIntOrNull())
                            }
                        },
                        label = { Text("Practical (Max ${subject.maxPractical})", color = animatedOnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f).onFocusEvent { practicalFocused = it.isFocused },
                        singleLine = true,
                        isError = (practicalText.toIntOrNull() ?: 0) > subject.maxPractical,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = animatedOnSurface,
                            unfocusedTextColor = animatedOnSurface,
                            focusedBorderColor = animatedPrimary,
                            unfocusedBorderColor = animatedOnSurfaceVariant,
                            focusedLabelColor = animatedPrimary,
                            unfocusedLabelColor = animatedOnSurfaceVariant
                        )
                    )
                }
            }
            if (item.mark?.mcq == null && item.mark?.written == null && item.mark?.practical == null) {
                Text(
                    text = "No marks entered",
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedOnSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
