import sys

path = "app/src/main/java/com/abutorab/marks9b/ui/screens/MarksheetScreen.kt"
with open(path, "r") as f:
    content = f.read()

target = """                            val sr = rowSpec.subjectResult
                            val isFailed = sr?.letterGrade == "F"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    rowSpec.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (sr == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                LedgerValueCell(sr?.mcqMarks?.toString() ?: "-", Modifier.width(42.dp))
                                LedgerValueCell(sr?.writtenMarks?.toString() ?: "-", Modifier.width(42.dp))
                                LedgerValueCell(sr?.practicalMarks?.toString() ?: "-", Modifier.width(42.dp))
                                LedgerValueCell(
                                    if (sr == null || sr.total == 0) "-" else "${sr.total}",
                                    Modifier.width(46.dp),
                                    color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    bold = sr != null
                                )
                                LedgerValueCell(
                                    sr?.letterGrade?.ifEmpty { "-" } ?: "-",
                                    Modifier.width(44.dp),
                                    color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                    bold = sr != null
                                )
                            }"""

replacement = """                            val sr = rowSpec.subjectResult
                            val isFailed = sr?.letterGrade == "F"
                            val componentCount = sr?.subject?.let { listOfNotNull(it.mcqMax, it.writtenMax, it.practicalMax).size } ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    rowSpec.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (sr == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                LedgerValueCell(
                                    formatMark(sr?.mcqMarks), Modifier.width(42.dp),
                                    color = if (isComponentFailed(sr?.mcqMarks, sr?.subject?.mcqMax, componentCount, isFailed)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                LedgerValueCell(
                                    formatMark(sr?.writtenMarks), Modifier.width(42.dp),
                                    color = if (isComponentFailed(sr?.writtenMarks, sr?.subject?.writtenMax, componentCount, isFailed)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                LedgerValueCell(
                                    formatMark(sr?.practicalMarks), Modifier.width(42.dp),
                                    color = if (isComponentFailed(sr?.practicalMarks, sr?.subject?.practicalMax, componentCount, isFailed)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                LedgerValueCell(
                                    if (sr == null || sr.total == 0) "-" else formatMark(sr.total),
                                    Modifier.width(46.dp),
                                    color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    bold = sr != null
                                )
                                LedgerValueCell(
                                    sr?.letterGrade?.ifEmpty { "-" } ?: "-",
                                    Modifier.width(44.dp),
                                    color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                    bold = sr != null
                                )
                            }"""

if target in content:
    content = content.replace(target, replacement)
    # Also replace the bottom total row padding
    content = content.replace("Row(\n                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),", "Row(\n                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),")
    with open(path, "w") as f:
        f.write(content)
    print("Replaced row block successfully.")
else:
    print("Target block not found.")
