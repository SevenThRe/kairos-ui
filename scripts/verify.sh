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
  "$ROOT/ui-render-opengl/src/main/java" \
  "$ROOT/ui-preview-svg/src/main/java" \
  "$ROOT/ui-preview-awt/src/main/java" \
  "$ROOT/platform-1.12.2-forge/src/main/java" \
  "$ROOT/platform-1.20.1-common/src/main/java" \
  "$ROOT/examples/modern-clickgui/src/main/java" \
  -name '*.java' -print | sort)

"${JAVAC[@]}" --release 8 -encoding UTF-8 -d "$MAIN" "${MAIN_SOURCES[@]}"

mapfile -t TEST_SOURCES < <(find \
  "$ROOT/ui-core/src/test/java" \
  "$ROOT/ui-components/src/test/java" \
  "$ROOT/ui-render-opengl/src/test/java" \
  "$ROOT/platform-1.12.2-forge/src/test/java" \
  "$ROOT/platform-1.20.1-common/src/test/java" \
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
java -cp "$MAIN:$TEST" dev.kairos.ui.render.opengl.RenderPipelineTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.legacy.Legacy112AdapterTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.modern.Modern120AdapterTest
java -cp "$MAIN" dev.kairos.ui.example.WorkbenchExample
java -cp "$MAIN" dev.kairos.ui.example.PreviewGenerator "$OUT/previews"
java -Djava.awt.headless=true -cp "$MAIN" dev.kairos.ui.example.RasterPreviewGenerator "$OUT/previews"
python3 "$ROOT/scripts/preprocess-smoke.py" "$ROOT" "$OUT/preprocess"
rg -q 'extends GuiScreen' "$OUT/preprocess/11202/dev/kairos/ui/minecraft/KairosScreen.java"
! rg -q 'GuiGraphics|Minecraft.getInstance' "$OUT/preprocess/11202/dev/kairos/ui/minecraft"
rg -q 'extends Screen' "$OUT/preprocess/12001/dev/kairos/ui/minecraft/KairosScreen.java"
! rg -q 'GuiScreen|Minecraft.getMinecraft|org.lwjgl' "$OUT/preprocess/12001/dev/kairos/ui/minecraft"
test -s "$OUT/previews/workbench.svg"
test -s "$OUT/previews/panel-desktop.svg"
test -s "$OUT/previews/hud.svg"
test -s "$OUT/previews/workbench.png"
test -s "$OUT/previews/panel-desktop.png"
test -s "$OUT/previews/hud.png"
echo "Kairos UI Engine verification passed"
