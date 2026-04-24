#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SETTINGS_FILE="${1:-$PROJECT_DIR/client-settings.json}"
if [[ ! -f "$SETTINGS_FILE" ]]; then
  echo "Settings file not found: $SETTINGS_FILE" >&2
  exit 1
fi
"$SCRIPT_DIR/build.sh"
java -cp "$PROJECT_DIR/out/main" de.sasd.mustelalog.client.app.MustelaLogClientApplication "$SETTINGS_FILE"
