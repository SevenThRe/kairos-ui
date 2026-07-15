#!/usr/bin/env python3
"""Small directive smoke check; the authoritative preprocessing remains EGT in CI."""
from pathlib import Path
import re
import sys


def evaluate(expression: str, mc: int) -> bool:
    match = re.fullmatch(r"MC\s*(>=|<=|==|>|<)\s*(\d+)", expression.strip())
    if not match:
        raise ValueError("Unsupported preprocessor expression: " + expression)
    op, raw = match.groups()
    value = int(raw)
    return {">=": mc >= value, "<=": mc <= value, "==": mc == value,
            ">": mc > value, "<": mc < value}[op]


def transform(source: str, mc: int) -> str:
    output = []
    stack = []
    active = True
    for number, line in enumerate(source.splitlines(), 1):
        stripped = line.strip()
        if stripped.startswith("//#if "):
            condition = evaluate(stripped[6:], mc)
            stack.append([active, condition])
            active = active and condition
        elif stripped.startswith("//#elseif "):
            if not stack:
                raise ValueError("elseif without if at line %d" % number)
            parent, matched = stack[-1]
            condition = not matched and evaluate(stripped[10:], mc)
            stack[-1] = [parent, matched or condition]
            active = parent and condition
        elif stripped == "//#else":
            if not stack:
                raise ValueError("else without if at line %d" % number)
            parent, matched = stack[-1]
            condition = not matched
            stack[-1] = [parent, True]
            active = parent and condition
        elif stripped == "//#endif":
            if not stack:
                raise ValueError("endif without if at line %d" % number)
            parent, _ = stack.pop()
            active = parent
        elif active:
            marker = line.find("//$$ ")
            output.append(line[:marker] + line[marker + 5:] if marker >= 0 else line)
    if stack:
        raise ValueError("Unclosed preprocessor block")
    return "\n".join(output) + "\n"


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: preprocess-smoke.py <engine-root> <output-dir>")
    root = Path(sys.argv[1])
    output = Path(sys.argv[2])
    source_root = root / "minecraft-build/src/main/java"
    for mc in (11202, 12001):
        target = output / str(mc)
        for source in source_root.rglob("*.java"):
            result = transform(source.read_text(encoding="utf-8"), mc)
            destination = target / source.relative_to(source_root)
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_text(result, encoding="utf-8")


if __name__ == "__main__":
    main()
