package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.marks9b.ui.MarksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(termId: Int, viewModel: MarksViewModel, onBack: () -> Unit) {
    val term by viewModel.getTermById(termId).collectAsStateWithLifecycle(initialValue = null)
    val currentTerm = term ?: return
    val students by viewModel.getStudentsForYear(currentTerm.yearId).collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.getSubjectsForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())
    val marks by viewModel.getMarksForTerm(termId).collectAsStateWithLifecycle(initialValue = emptyList())

    val results = remember(students, subjects, marks) { TabulationEngine.compute(students, subjects, marks) }
    val stats = remember(results) { DashboardEngine.compute(results) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard — ${currentTerm.label}", style = MaterialTheme.typography.titleMedium) },
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
        if (students.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DateRange, contentDescription = "No data", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No students yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SectionLabel("Core Summary") }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard("Total Students", stats.totalStudents.toString(), Modifier.weight(1f))
                            StatCard("Passed", stats.passed.toString(), Modifier.weight(1f), accentColor = MaterialTheme.colorScheme.tertiary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard("Failed", stats.failed.toString(), Modifier.weight(1f), accentColor = MaterialTheme.colorScheme.error)
                            StatCard("Pass Rate", "%.1f%%".format(stats.passRate), Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard("Average Total", "%.1f".format(stats.averageTotal), Modifier.weight(1f))
                            StatCard("Median Total", "%.1f".format(stats.medianTotal), Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard("Highest Mark", stats.highestTotal.toString(), Modifier.weight(1f))
                            StatCard("Class Avg GPA", "%.2f".format(stats.averageGpa), Modifier.weight(1f))
                        }
                        if (stats.ungradedStudents > 0) {
                            Text(
                                "${stats.ungradedStudents} student(s) not yet graded — excluded from the stats above",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { SectionLabel("Performance Distribution") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Students per Grade", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            GradeBarChart(stats.gradeDistribution, MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item { SectionLabel("Total Marks by Roll") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MarksLineChart(stats.marksByRoll, MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item { SectionLabel("Subject-wise Deep Analysis") }
                if (stats.hardestSubjects.isNotEmpty()) {
                    item {
                        val names = stats.hardestSubjects.joinToString(", ") { bengaliSubjectLabel(it.subjectName) }
                        val avgPercent = stats.hardestSubjects.first().let { (it.average / it.fullMarks) * 100.0 }
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Hardest Subject: $names (%.1f%% Avg)".format(avgPercent),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
                item { SubjectStatsTable(stats.subjectStats) }

                item { SectionLabel("Ranking & Merit") }
                item { LeaderboardTable("Top 10 Students", stats.rankedStudents.take(10), showFailedInstead = false) }
                item { LeaderboardTable("Bottom 10 Students", stats.rankedStudents.takeLast(10).reversed(), showFailedInstead = true) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, accentColor: Color? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accentColor ?: MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun GradeBarChart(distribution: Map<String, Int>, barColor: Color) {
    val maxValue = (distribution.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        distribution.forEach { (grade, count) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    val fraction = count.toFloat() / maxValue.toFloat()
                    Canvas(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight(fraction.coerceIn(0.02f, 1f))) {
                        drawRect(color = barColor)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text(grade, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = labelColor)
            }
        }
    }
}

@Composable
private fun MarksLineChart(data: List<Pair<Int, Int>>, lineColor: Color) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    if (data.isEmpty()) return
    val maxTotal = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val stepX = if (data.size > 1) size.width / (data.size - 1) else 0f
        val points = data.mapIndexed { index, (_, total) ->
            val x = index * stepX
            val y = size.height - (total.toFloat() / maxTotal.toFloat()) * size.height
            Offset(x, y)
        }
        for (i in 0 until points.size - 1) {
            drawLine(color = lineColor, start = points[i], end = points[i + 1], strokeWidth = 4f)
        }
        points.forEach { point ->
            drawCircle(color = lineColor, radius = 6f, center = point)
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        data.forEach { (roll, _) ->
            Text(roll.toString(), style = MaterialTheme.typography.labelSmall, color = labelColor)
        }
    }
}

@Composable
private fun SubjectStatsTable(subjects: List<SubjectStats>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                TableHeaderCell("Subject", Modifier.weight(1f), TextAlign.Start)
                TableHeaderCell("Avg", Modifier.width(56.dp))
                TableHeaderCell("High", Modifier.width(44.dp))
                TableHeaderCell("Low", Modifier.width(44.dp))
                TableHeaderCell("Pass", Modifier.width(40.dp))
                TableHeaderCell("Fail", Modifier.width(40.dp))
            }
            HorizontalDivider()
            subjects.forEachIndexed { index, s ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(bengaliSubjectLabel(s.subjectName), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TableValueCell("%.1f".format(s.average), Modifier.width(56.dp))
                    TableValueCell(s.highest.toString(), Modifier.width(44.dp))
                    TableValueCell(s.lowest.toString(), Modifier.width(44.dp))
                    TableValueCell(s.passed.toString(), Modifier.width(40.dp), color = MaterialTheme.colorScheme.tertiary)
                    TableValueCell(s.failed.toString(), Modifier.width(40.dp), color = if (s.failed > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }
                if (index < subjects.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}

@Composable
private fun LeaderboardTable(title: String, students: List<StudentRanking>, showFailedInstead: Boolean) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    TableHeaderCell("Rank", Modifier.width(40.dp))
                    TableHeaderCell("Roll", Modifier.width(44.dp))
                    TableHeaderCell("Name", Modifier.weight(1f), TextAlign.Start)
                    TableHeaderCell("Total", Modifier.width(52.dp))
                    TableHeaderCell(if (showFailedInstead) "Failed" else "GPA", Modifier.width(48.dp))
                }
                HorizontalDivider()
                students.forEachIndexed { index, s ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        TableValueCell((index + 1).toString(), Modifier.width(40.dp), bold = true)
                        TableValueCell(s.roll.toString(), Modifier.width(44.dp))
                        Text(s.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        TableValueCell(s.total.toString(), Modifier.width(52.dp), bold = true)
                        if (showFailedInstead) {
                            TableValueCell(s.failedCount.toString(), Modifier.width(48.dp), color = if (s.failedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                        } else {
                            TableValueCell(s.gpa?.let { "%.2f".format(it) } ?: "-", Modifier.width(48.dp), color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    if (index < students.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center) {
    Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = align, modifier = modifier)
}

@Composable
private fun TableValueCell(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, bold: Boolean = false) {
    Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color, textAlign = TextAlign.Center, modifier = modifier)
}

private fun bengaliSubjectLabel(name: String): String = when (name) {
    "Bangla 1st Paper" -> "বাংলা ১ম"
    "Bangla 2nd Paper" -> "বাংলা ২য়"
    "English 1st Paper" -> "ইংরেজি ১ম"
    "English 2nd Paper" -> "ইংরেজি ২য়"
    "Mathematics" -> "গণিত"
    "Islam Religion and Moral Education" -> "ইসলাম"
    "Hindu Religion and Moral Education" -> "হিন্দু"
    "Buddhist Religion and Moral Education" -> "বৌদ্ধ"
    "Bangladesh & Global Studies" -> "বাংলাদেশ ও বিশ্বপরিচয়"
    "General Science" -> "সাধারণ বিজ্ঞান"
    "Physics" -> "পদার্থ বিজ্ঞান"
    "Chemistry" -> "রসায়ন বিজ্ঞান"
    "Biology" -> "জীব বিজ্ঞান"
    "Accounting" -> "হিসাব বিজ্ঞান"
    "Finance" -> "ফাইন্যান্স ও ব্যাংকিং"
    "B. Entrepreneurship" -> "ব্যবসায় উদ্যোগ"
    "History" -> "ইতিহাস"
    "Geography" -> "ভূগোল"
    "Civics" -> "পৌরনীতি"
    "Higher Mathematics" -> "উচ্চতর গণিত"
    "Agriculture Studies" -> "কৃষি শিক্ষা"
    "Information & Communication Technology" -> "তথ্য ও যোগাযোগ প্রযুক্তি"
    else -> name
}
