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
  "$ROOT/ui-esp/src/main/java" \
  "$ROOT/platform-api/src/main/java" \
  "$ROOT/ui-render-opengl/src/main/java" \
  "$ROOT/web-surface-core/src/main/java" \
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
  "$ROOT/ui-esp/src/test/java" \
  "$ROOT/ui-render-opengl/src/test/java" \
  "$ROOT/web-surface-core/src/test/java" \
  "$ROOT/platform-api/src/test/java" \
  "$ROOT/platform-1.12.2-forge/src/test/java" \
  "$ROOT/platform-1.20.1-common/src/test/java" \
  -name '*.java' -print | sort)

"${JAVAC[@]}" --release 8 -encoding UTF-8 -cp "$MAIN" -d "$TEST" "${TEST_SOURCES[@]}"

FORBIDDEN='import (net\.minecraft|net\.minecraftforge|net\.fabricmc|org\.lwjgl|com\.mojang)'
if command -v rg >/dev/null 2>&1; then
  FORBIDDEN_MATCHES=$(rg -n "$FORBIDDEN" \
    "$ROOT/ui-api/src" "$ROOT/ui-core/src" "$ROOT/ui-components/src" "$ROOT/ui-esp/src" || true)
else
  FORBIDDEN_MATCHES=$(grep -R -n -E "$FORBIDDEN" \
    "$ROOT/ui-api/src" "$ROOT/ui-core/src" "$ROOT/ui-components/src" "$ROOT/ui-esp/src" || true)
fi
if [[ -n "$FORBIDDEN_MATCHES" ]]; then
  echo "$FORBIDDEN_MATCHES"
  echo "Forbidden platform import detected in shared UI modules" >&2
  exit 1
fi

java -cp "$MAIN:$TEST" dev.kairos.ui.core.CoreSmokeTest
java -cp "$MAIN:$TEST" dev.kairos.ui.theme.ThemeSystemTest
java -cp "$MAIN:$TEST" dev.kairos.ui.components.ComponentSmokeTest
java -cp "$MAIN:$TEST" dev.kairos.ui.components.hud.NotificationCenterTest
java -cp "$MAIN:$TEST" dev.kairos.ui.components.hud.CombatHudProfileTest
java -cp "$MAIN:$TEST" dev.kairos.ui.esp.EspRendererTest
java -cp "$MAIN:$TEST" dev.kairos.ui.esp.EspStyleCodecTest
java -cp "$MAIN:$TEST" dev.kairos.ui.render.opengl.RenderPipelineTest
java -cp "$MAIN:$TEST" dev.kairos.ui.web.PixelFrameMailboxTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.KairosGuiActivationTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.KairosClientCommandTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.ThemeDirectoryTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.legacy.Legacy112AdapterTest
java -cp "$MAIN:$TEST" dev.kairos.ui.platform.modern.Modern120AdapterTest
java -cp "$MAIN" dev.kairos.ui.example.PreviewGenerator "$OUT/previews"
java -Djava.awt.headless=true -cp "$MAIN" dev.kairos.ui.example.RasterPreviewGenerator "$OUT/previews"
INTEGRATION="$ROOT/integrations/liquidbounce-1.12.2"
test -s "$INTEGRATION/SOURCE.lock"
test -s "$INTEGRATION/overlay/shared/main/resources/assets/kairos_ui/web/index.html"
test -s "$INTEGRATION/overlay/shared/main/resources/assets/kairos_ui/web/app.css"
test -s "$INTEGRATION/overlay/shared/main/resources/assets/kairos_ui/web/app.js"
test -s "$INTEGRATION/overlay/1.12.2-Forge/src/main/java/dev/kairos/ui/liquidbounce/KairosWebBridge.java"
test ! -e "$ROOT/minecraft-build"
if command -v node >/dev/null 2>&1; then
  node --check "$INTEGRATION/overlay/shared/main/resources/assets/kairos_ui/web/app.js"
fi
test -s "$OUT/previews/hud.svg"
test -s "$OUT/previews/esp.svg"
test -s "$OUT/previews/hud.png"
test -s "$OUT/previews/esp.png"
test -s "$OUT/previews/combat-hud.png"
test -s "$OUT/previews/competitive-esp.png"
echo "Kairos UI Engine verification passed"
