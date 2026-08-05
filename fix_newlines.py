import sys

def remove_empty_lines(content):
    lines = content.split('\n')
    out_lines = []
    in_block = False
    for line in lines:
        if "val rank = studentResults.count {" in line:
            in_block = True
        if in_block and line.strip() == "":
            continue
        if in_block and "} + 1" in line:
            in_block = False
        out_lines.append(line)
    return '\n'.join(out_lines)

path = "app/src/main/java/com/abutorab/marks9b/ui/screens/TabulationEngine.kt"
with open(path, "r") as f:
    content = f.read()

with open(path, "w") as f:
    f.write(remove_empty_lines(content))
