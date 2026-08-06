import sys

path = "app/src/main/java/com/abutorab/marks9b/ui/screens/MarksheetScreen.kt"
with open(path, "r") as f:
    content = f.read()

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

if "private fun formatMark" not in content:
    content += helpers
    with open(path, "w") as f:
        f.write(content)
    print("Helpers added.")
else:
    print("Helpers already exist.")
