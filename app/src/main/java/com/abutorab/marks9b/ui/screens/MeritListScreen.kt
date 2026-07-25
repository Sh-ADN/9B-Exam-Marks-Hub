@file:OptIn(ExperimentalMaterial3Api::class)

package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.marks9b.data.local.entity.OptionalType
import com.abutorab.marks9b.data.local.entity.Religion
import com.abutorab.marks9b.data.local.entity.StudentGroup
import com.abutorab.marks9b.ui.MarksViewModel

private val topNOptions = listOf(5, 10, 20, 0) // 0 == "All"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeritListScreen(termId: Int, viewModel: MarksViewModel, onBack: () -> Unit) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    val currentTerm = term ?: return
    val students by viewModel.getStudentsForYear(currentTerm.yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    val marks by viewModel.getMarksForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    val results = remember(students, subjects, marks) { TabulationEngine.compute(students, subjects, marks) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Merit List — ${currentTerm.label}", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = { com.abutorab.marks9b.ui.components.ThemeToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Merit List") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Subject Toppers") })
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (students.isEmpty()) {
                EmptyState()
            } else if (selectedTab == 0) {
                MeritListTab(results)
            } else {
                SubjectToppersTab(results)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EmojiEvents, contentDescription = "No data", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("No students yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ---------------------------------------------------------------------
// Merit List tab
// ---------------------------------------------------------------------

private val groupChoices = listOf(null to "All") + StudentGroup.values().map { it.code to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
private val religionChoices = listOf(null to "All") + Religion.values().map { it.code to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
private val optionalChoices = listOf(null to "All") + OptionalType.values().map {
    it.code to if (it == OptionalType.HIGHER_MATH) "Higher Math" else "Agriculture"
}

@Composable
private fun MeritListTab(results: List<StudentResult>) {
    var group by remember { mutableStateOf<String?>(null) }
    var religion by remember { mutableStateOf<String?>(null) }
    var optionalType by remember { mutableStateOf<String?>(null) }
    var excludeFailed by remember { mutableStateOf(true) }
    var topN by remember { mutableIntStateOf(10) }

    val filter = remember(group, religion, optionalType, excludeFailed, topN) {
        MeritListFilter(
            group = group,
            religion = religion,
            optionalType = optionalType,
            excludeFailed = excludeFailed,
            topN = if (topN == 0) null else topN
        )
    }
    val entries = remember(results, filter) { MeritListEngine.generateMeritList(results, filter) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            FilterPanel(
                group = group, onGroupChange = { group = it },
                religion = religion, onReligionChange = { religion = it },
                optionalType = optionalType, onOptionalChange = { optionalType = it },
                excludeFailed = excludeFailed, onExcludeFailedChange = { excludeFailed = it },
                topN = topN, onTopNChange = { topN = it }
            )
        }
        item {
            Text(
                "${entries.size} student(s) shown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (entries.isEmpty()) {
            item {
                Text(
                    "No students match these filters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            item { MeritTable(entries) }
        }
    }
}

@Composable
private fun FilterPanel(
    group: String?, onGroupChange: (String?) -> Unit,
    religion: String?, onReligionChange: (String?) -> Unit,
    optionalType: String?, onOptionalChange: (String?) -> Unit,
    excludeFailed: Boolean, onExcludeFailedChange: (Boolean) -> Unit,
    topN: Int, onTopNChange: (Int) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Group", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ChipRow(groupChoices, group, onGroupChange)

            Text("Top N", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ChipRow(topNOptions.map { it to if (it == 0) "All" else "Top $it" }, topN, onTopNChange)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Exclude failed students", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = excludeFailed, onCheckedChange = onExcludeFailedChange)
            }

            var advancedExpanded by remember { mutableStateOf(false) }
            TextButton(onClick = { advancedExpanded = !advancedExpanded }) {
                Text(if (advancedExpanded) "Hide advanced filters" else "More filters (religion, optional subject)")
            }
            if (advancedExpanded) {
                Text("Religion", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipRow(religionChoices, religion, onReligionChange)
                Text("Optional Subject", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipRow(optionalChoices, optionalType, onOptionalChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChipRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun MeritTable(entries: List<MeritListEntry>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                TableHeaderCellM("Rank", Modifier.width(44.dp))
                TableHeaderCellM("Roll", Modifier.width(44.dp))
                TableHeaderCellM("Name", Modifier.weight(1f), TextAlign.Start)
                TableHeaderCellM("Grp", Modifier.width(36.dp))
                TableHeaderCellM("Total", Modifier.width(52.dp))
                TableHeaderCellM("GPA", Modifier.width(48.dp))
            }
            HorizontalDivider()
            entries.forEachIndexed { index, e ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val medalColor = when (e.rank) {
                        1 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    TableValueCellM(e.rank.toString(), Modifier.width(44.dp), bold = true, color = medalColor)
                    TableValueCellM(e.roll.toString(), Modifier.width(44.dp))
                    Text(e.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TableValueCellM(e.group, Modifier.width(36.dp))
                    TableValueCellM(e.grandTotal.toString(), Modifier.width(52.dp), bold = true)
                    TableValueCellM(
                        e.gpa?.let { "%.2f".format(it) } ?: "-",
                        Modifier.width(48.dp),
                        color = if (e.failedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
                if (index < entries.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------
// Subject Toppers tab
// ---------------------------------------------------------------------

private val subjectTopperNOptions = listOf(1, 3, 5)

@Composable
private fun SubjectToppersTab(results: List<StudentResult>) {
    var topN by remember { mutableIntStateOf(3) }
    val groups = remember(results, topN) { MeritListEngine.generateSubjectToppers(results, topN) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Toppers per subject", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    ChipRow(subjectTopperNOptions.map { it to "Top $it" }, topN, onSelect = { topN = it })
                }
            }
        }
        if (groups.isEmpty()) {
            item {
                Text(
                    "No graded subjects yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(groups, key = { "${it.sheetRole}_${it.applicabilityValue}" }) { subjectGroup ->
                SubjectTopperCard(subjectGroup)
            }
        }
    }
}

@Composable
private fun SubjectTopperCard(group: SubjectTopperGroup) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    group.subjectName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text("/${group.fullMarks}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            if (group.toppers.isEmpty()) {
                Text("No marks entered yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                group.toppers.forEachIndexed { index, topper ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "#${topper.rank}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (topper.rank == 1) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(32.dp)
                        )
                        Text("Roll ${topper.roll}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                        Text(topper.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(topper.breakdown, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)) {
                            Text(topper.letterGrade, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    if (index < group.toppers.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// Small shared table cell helpers (kept local to this file to avoid
// touching DashboardScreen's private ones)
// ---------------------------------------------------------------------

@Composable
private fun TableHeaderCellM(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center) {
    Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = align, modifier = modifier)
}

@Composable
private fun TableValueCellM(text: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface, bold: Boolean = false) {
    Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color, textAlign = TextAlign.Center, modifier = modifier)
}
