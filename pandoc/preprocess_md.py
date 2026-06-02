import sys, re, os

input_file, stem, output_file = sys.argv[1], sys.argv[2], sys.argv[3]
input_dir = os.path.dirname(os.path.abspath(input_file))

with open(input_file, encoding="utf-8") as f:
    content = f.read()

blocks = re.findall(r'```plantuml\n.*?```', content, re.DOTALL)

result = content
for i, block in enumerate(blocks):
    name = stem if i == 0 else f"{stem}_{i:03d}"
    img_path = os.path.join(input_dir, f"{name}.png")
    img = f"\\begin{{center}}\n\\includegraphics{{{img_path}}}\n\\end{{center}}"
    result = result.replace(block, img, 1)

with open(output_file, "w", encoding="utf-8") as f:
    f.write(result)

print(f"Replaced {len(blocks)} PlantUML block(s) with image references.")
