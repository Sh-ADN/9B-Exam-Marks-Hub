cat << 'INNER' > replace_merit.py
import sys

path = "app/src/main/java/com/abutorab/marks9b/ui/screens/MeritListEngine.kt"
with open(path, "r") as f:
    content = f.read()

target = """                val betterCount = eligible.count {
                    it.failedCount < result.failedCount ||
                        (it.failedCount == result.failedCount && it.grandTotal > result.grandTotal)
                }"""

replacement = """                val betterCount = eligible.count {
                    if (it.failedCount < result.failedCount) {
                        true
                    } else if (it.failedCount == result.failedCount) {
                        if (result.failedCount == 0) {
                            val itGpa = it.gpa ?: 0.0
                            val resultGpa = result.gpa ?: 0.0
                            (itGpa > resultGpa) || (itGpa == resultGpa && it.grandTotal > result.grandTotal)
                        } else {
                            it.grandTotal > result.grandTotal
                        }
                    } else {
                        false
                    }
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("Replaced in MeritListEngine")
else:
    print("Target not found in MeritListEngine")
INNER
python3 replace_merit.py
