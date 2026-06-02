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
    python3 "${SCRIPT_DIR}/preprocess_md.py" "$input" "$stem" "$output"
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
    local input="$1" output="$2" resource_path="$3"
    local args=("$input" --from markdown --defaults "$DEFAULTS_FILE" "--resource-path=${resource_path}" -o "$output" -H "${SCRIPT_DIR}/header.tex")

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
    trap "rm -f '$tmpfile'" EXIT

    generate_diagrams "$input"
    preprocess_md     "$input" "$stem" "$tmpfile"
    build_pdf         "$tmpfile" "$output" "$input_dir"
    cleanup_diagrams  "$input_dir" "$stem"
    echo "Done."
}

main "$@"
