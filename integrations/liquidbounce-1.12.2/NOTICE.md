# Kairos + LiquidBounce 1.12.2 distribution notice

This integration produces a combined Forge 1.12.2 client based on the GPL-3.0
LiquidBounce b73 1.12.2 port pinned in `SOURCE.lock`. The integration overlay and
the resulting combined distribution are provided under GPL-3.0. The independent
Kairos engine outside this directory retains the repository's root license.

Upstream projects:

- LiquidBounce: https://github.com/CCBlueX/LiquidBounce
- Pinned 1.12.2 port: https://github.com/Kamiya1337/LiquidBounce-1.12.2
- MCEF 1.12.2-1.11: https://github.com/montoyo/mcef/releases/tag/1.12.2-1.11
- JCEF: https://github.com/chromiumembedded/java-cef

The build embeds the upstream GPL license as
`META-INF/LICENSE-LiquidBounce-GPL-3.0.txt` in the final JAR and embeds this
notice as `META-INF/NOTICE-Kairos-LiquidBounce.md`.

