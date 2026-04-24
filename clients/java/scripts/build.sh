#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="$PROJECT_DIR/out/main"
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
find "$PROJECT_DIR/src/main/java" -name "*.java" | sort | xargs javac -encoding UTF-8 -d "$OUT_DIR"
echo "Build successful: $OUT_DIR"
