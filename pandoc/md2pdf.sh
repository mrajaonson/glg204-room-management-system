#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PLANTUML_JAR="${SCRIPT_DIR}/plantuml.jar"
DEFAULTS_FILE="${SCRIPT_DIR}/defaults.yaml"

check_deps() {
    for cmd in pandoc python3; do
        command -v "$cmd" &>/dev/null || { echo "Error: '$cmd' not found in PATH" >&2; exit 1; }
    done
}

generate_diagrams() {
    local input="$1"
    if ! grep -q '```plantuml' "$input"; then
        echo "No PlantUML blocks — skipping diagram generation."
        return
    fi
    command -v java &>/dev/null || { echo "Error: 'java' not found in PATH" >&2; exit 1; }
    [[ -f "$PLANTUML_JAR" ]] || { echo "Error: plantuml.jar not found at '$PLANTUML_JAR'" >&2; exit 1; }
    echo "Generating PlantUML diagrams..."
    java -jar "$PLANTUML_JAR" "$input"
}

preprocess_md() {
    local input="$1" stem="$2" output="$3"
    python3 - "$input" "$stem" "$output" <<'PYEOF'
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
    img = f"\\begin{{center}}\n\\includegraphics[width=\\linewidth,height=0.9\\textheight,keepaspectratio]{{{img_path}}}\n\\end{{center}}"
    result = result.replace(block, img, 1)

with open(output_file, "w", encoding="utf-8") as f:
    f.write(result)

print(f"Replaced {len(blocks)} PlantUML block(s) with image references.")
PYEOF
}

cleanup_diagrams() {
    local dir="$1" stem="$2"
    local -a files
    shopt -s nullglob
    files=("${dir}/${stem}.png" "${dir}/${stem}"_*.png)
    shopt -u nullglob
    if [[ ${#files[@]} -gt 0 ]]; then
        rm -f "${files[@]}"
        echo "Cleaned ${#files[@]} diagram PNG(s)."
    fi
}

build_pdf() {
    local input="$1" output="$2" resource_path="$3" hdrfile="$4"
    local args=("$input" --from markdown --defaults "$DEFAULTS_FILE" "--resource-path=${resource_path}" -o "$output" -H "$hdrfile")

    echo "Building PDF → $output"
    pandoc "${args[@]}"
}

main() {
    [[ $# -lt 1 ]] && { echo "Error: input file required" >&2; exit 1; }
    [[ -f "$1" ]]  || { echo "Error: '$1' not found" >&2; exit 1; }

    local input; input="$(realpath "$1")"
    local input_dir; input_dir="$(dirname "$input")"
    local stem; stem="$(basename "${input%.md}")"
    local output="${2:-${input_dir}/${stem}.pdf}"

    check_deps

    local tmpfile="${input_dir}/.md2pdf_${stem}_$$.md"
    local hdrfile; hdrfile="$(mktemp --suffix=.tex)"
    cat > "$hdrfile" <<'TEX'
\renewcommand*\contentsname{}
\usepackage{graphicx}
TEX
    trap "rm -f '$tmpfile' '$hdrfile'" EXIT

    generate_diagrams "$input"
    preprocess_md     "$input" "$stem" "$tmpfile"
    build_pdf         "$tmpfile" "$output" "$input_dir" "$hdrfile"
    cleanup_diagrams  "$input_dir" "$stem"
    echo "Done."
}

main "$@"
