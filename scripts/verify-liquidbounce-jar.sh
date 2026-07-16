#!/usr/bin/env bash
set -euo pipefail

JAR="${1:-}"
if [[ -z "$JAR" || ! -f "$JAR" ]]; then
  echo "usage: $0 <Kairos-LiquidBounce.jar>" >&2
  exit 2
fi

unzip -tq "$JAR"
ENTRIES="$(unzip -Z1 "$JAR")"

require_entry() {
  local entry="$1"
  if ! grep -Fxq "$entry" <<< "$ENTRIES"; then
    echo "missing required JAR entry: $entry" >&2
    exit 3
  fi
}

require_entry "net/ccbluex/liquidbounce/features/module/modules/combat/KillAura.class"
require_entry "net/ccbluex/liquidbounce/features/module/modules/render/ESP.class"
require_entry "net/ccbluex/liquidbounce/features/module/modules/world/Scaffold.class"
require_entry "dev/kairos/ui/liquidbounce/KairosBootstrap.class"
require_entry "dev/kairos/ui/liquidbounce/KairosScreen.class"
require_entry "dev/kairos/ui/liquidbounce/KairosWebBridge.class"
require_entry "net/montoyo/mcef/MCEF.class"
require_entry "net/montoyo/mcef/api/MCEFApi.class"
require_entry "org/cef/CefApp.class"
require_entry "assets/kairos_ui/web/index.html"
require_entry "assets/kairos_ui/web/app.css"
require_entry "assets/kairos_ui/web/app.js"
require_entry "assets/mcef/letsencryptauthorityx3.crt"
require_entry "mcef-coremod.jar"
require_entry "META-INF/LICENSE-LiquidBounce-GPL-3.0.txt"

MODULE_CLASSES="$(grep -c '^net/ccbluex/liquidbounce/features/module/modules/.\+\.class$' <<< "$ENTRIES")"
if (( MODULE_CLASSES < 100 )); then
  echo "expected the real LiquidBounce module set, found only $MODULE_CLASSES module classes" >&2
  exit 4
fi

MANIFEST="$(unzip -p "$JAR" META-INF/MANIFEST.MF)"
grep -Fq 'FMLCorePlugin: net.ccbluex.liquidbounce.injection.forge.TransformerLoader' <<< "$MANIFEST"
grep -Fq 'ContainedDeps: mcef-coremod.jar' <<< "$MANIFEST"

if grep -Fqi 'required-after:mcef' < <(unzip -p "$JAR" mcmod.info); then
  echo "combined JAR still declares MCEF as an external required mod" >&2
  exit 5
fi

COREMOD="$(mktemp)"
trap 'rm -f "$COREMOD"' EXIT
unzip -p "$JAR" mcef-coremod.jar > "$COREMOD"
unzip -tq "$COREMOD"
unzip -p "$COREMOD" META-INF/MANIFEST.MF | grep -Fq \
  'FMLCorePlugin: net.montoyo.mcef.coremod.ShutdownPatcher'

echo "Verified $JAR"
echo "LiquidBounce module classes: $MODULE_CLASSES"
sha256sum "$JAR"

