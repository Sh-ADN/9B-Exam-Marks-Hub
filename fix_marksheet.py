import sys

path = "app/src/main/java/com/abutorab/marks9b/ui/screens/MarksheetScreen.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Add helper functions at the bottom
helpers = """
private fun formatMark(mark: Int?): String {
    if (mark == null) return "-"
    return mark.toString().padStart(2, '0')
}

private fun isComponentFailed(mark: Int?, max: Int?, componentCount: Int, subjectFailed: Boolean): Boolean {
    if (max == null) return false
    if (componentCount <= 1) return subjectFailed
    val threshold = kotlin.math.round(max / 3.0).toInt()
    return (mark ?: 0) < threshold
}
"""

if "formatMark(" not in content:
    content += helpers

# 2. Update the padding of the row from 10.dp to 4.dp
content = content.replace("modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)", 
                          "modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)")

# 3. Update the LedgerValueCell calls to use formatMark and color logic
target_row_start = "val isFailed = sr?.letterGrade == \"F\""
# We need to find the block of code generating the row.
# Let's replace the whole block for each row.

old_row = """                            val sr = rowSpec.subjectResult
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

# Wait, the vertical padding is already replaced by the previous string replacement if I didn't do it right, let's just do it in one go.
