#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_MAIN="$PROJECT_DIR/out/main"
OUT_TEST="$PROJECT_DIR/out/test"
"$SCRIPT_DIR/build.sh"
rm -rf "$OUT_TEST"
mkdir -p "$OUT_TEST"
find "$PROJECT_DIR/src/test/java" -name "*.java" | sort | xargs javac -encoding UTF-8 -cp "$OUT_MAIN" -d "$OUT_TEST"
java -cp "$OUT_MAIN:$OUT_TEST" de.sasd.mustelalog.client.tests.TestRunner
