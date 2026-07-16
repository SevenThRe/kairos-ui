/*
 * Intentionally source-empty.
 *
 * The pinned 1.12.2 port contains both Java and Kotlin implementations with
 * the same AntiBot binary name. ModuleManager constructs the public Java
 * class via Class.newInstance(), so that implementation is authoritative.
 * Leaving the duplicate Kotlin object enabled creates two AntiBot.class
 * entries and makes ForgeGradle's reobfuscation fail.
 */
