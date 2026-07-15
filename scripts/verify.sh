#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/out/verify"
MAIN="$OUT/main"
TEST="$OUT/test"
rm -rf "$OUT"
mkdir -p "$MAIN" "$TEST"

if command -v javac >/dev/null 2>&1; then
  JAVAC=(javac)
else
  JAVAC=(java -m jdk.compiler/com.sun.tools.javac.Main)
fi

mapfile -t MAIN_SOURCES < <(find \
  "$ROOT/ui-api/src/main/java" \
  "$ROOT/ui-core/src/main/java" \
  "$ROOT/ui-components/src/main/java" \
  "$ROOT/platform-api/src/main/java" \
  "$ROOT/platform-1.12.2-forge/src/main/java" \
  "$ROOT/platform-1.20.1-common/src/main/java" \
  "$ROOT/examples/modern-clickgui/src/main/java" \
  -name '*.java' -print | sort)

"${JAVAC[@]}" --release 8 -encoding UTF-8 -d "$MAIN" "${MAIN_SOURCES[@]}"

mapfile -t TEST_SOURCES < <(find \
  "$ROOT/ui-core/src/test/java" \
  "$ROOT/ui-components/src/test/java" \
  -name '*.java' -print | sort)

"${JAVAC[@]}" --release 8 -encoding UTF-8 -cp "$MAIN" -d "$TEST" "${TEST_SOURCES[@]}"

FORBIDDEN='import (net\.minecraft|net\.minecraftforge|net\.fabricmc|org\.lwjgl|com\.mojang)'
if rg -n "$FORBIDDEN" \
  "$ROOT/ui-api/src" "$ROOT/ui-core/src" "$ROOT/ui-components/src"; then
  echo "Forbidden platform import detected in shared UI modules" >&2
  exit 1
fi

java -cp "$MAIN:$TEST" dev.kairos.ui.core.CoreSmokeTest
java -cp "$MAIN:$TEST" dev.kairos.ui.components.ComponentSmokeTest
java -cp "$MAIN" dev.kairos.ui.example.WorkbenchExample
echo "Kairos UI Engine verification passed"
