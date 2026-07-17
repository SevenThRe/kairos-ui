#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPOSITORY_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
UPSTREAM_ROOT="${1:-}"
MCEF_JAR="${2:-}"
EXPECTED_COMMIT="23e11be9b078a931edc68078f4be62a1c48724a5"
EXPECTED_MCEF_SHA256="fdc0842c952884ae12fe179c00d66e8eacc938c08bd816a86ec347bca52e94ef"

if [[ -z "$UPSTREAM_ROOT" || -z "$MCEF_JAR" ]]; then
  echo "usage: $0 <liquidbounce-1.12.2-root> <mcef-api-jar>" >&2
  exit 2
fi

if ! git -C "$UPSTREAM_ROOT" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "LiquidBounce checkout not found: $UPSTREAM_ROOT" >&2
  exit 2
fi

ACTUAL_COMMIT="$(git -C "$UPSTREAM_ROOT" rev-parse HEAD)"
if [[ "$ACTUAL_COMMIT" != "$EXPECTED_COMMIT" ]]; then
  echo "LiquidBounce revision mismatch: expected $EXPECTED_COMMIT, got $ACTUAL_COMMIT" >&2
  exit 3
fi

if [[ ! -f "$MCEF_JAR" ]]; then
  echo "MCEF API JAR not found: $MCEF_JAR" >&2
  exit 2
fi

ACTUAL_MCEF_SHA256="$(sha256sum "$MCEF_JAR" | awk '{print $1}')"
if [[ "$ACTUAL_MCEF_SHA256" != "$EXPECTED_MCEF_SHA256" ]]; then
  echo "MCEF checksum mismatch: expected $EXPECTED_MCEF_SHA256, got $ACTUAL_MCEF_SHA256" >&2
  exit 4
fi

mkdir -p "$UPSTREAM_ROOT/1.12.2-Forge/libs"
cp "$MCEF_JAR" "$UPSTREAM_ROOT/1.12.2-Forge/libs/mcef-1.12.2-1.11-api.jar"
cp -R "$SCRIPT_DIR/overlay/1.12.2-Forge/." "$UPSTREAM_ROOT/1.12.2-Forge/"
cp -R "$SCRIPT_DIR/overlay/shared/." "$UPSTREAM_ROOT/shared/"
cp -R "$REPOSITORY_ROOT/web-surface-core/src/main/java/." "$UPSTREAM_ROOT/shared/main/java/"
mkdir -p "$UPSTREAM_ROOT/shared/main/resources/META-INF"
cp "$SCRIPT_DIR/NOTICE.md" "$UPSTREAM_ROOT/shared/main/resources/META-INF/NOTICE-Kairos-LiquidBounce.md"

patch --batch --forward --no-backup-if-mismatch -d "$UPSTREAM_ROOT" -p1 < "$SCRIPT_DIR/patches/command-manager.patch"
patch --batch --forward --no-backup-if-mismatch -d "$UPSTREAM_ROOT" -p1 < "$SCRIPT_DIR/patches/thealtening-modern-api.patch"
patch --batch --forward --no-backup-if-mismatch -d "$UPSTREAM_ROOT" -p1 < "$SCRIPT_DIR/patches/upstream-build-fixes.patch"

echo "Prepared Kairos LiquidBounce integration at $UPSTREAM_ROOT"
