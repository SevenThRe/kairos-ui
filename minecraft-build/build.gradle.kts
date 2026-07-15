import gg.essential.gradle.util.noServerRunConfigs

plugins {
    id("gg.essential.defaults")
    id("gg.essential.multi-version")
}

group = "dev.kairos.ui"
version = rootProject.version

loom.noServerRunConfigs()
java.toolchain.languageVersion.set(JavaLanguageVersion.of(if (platform.mcVersion < 11700) 8 else 17))

val engineRoot = rootProject.projectDir.parentFile
sourceSets.main {
    java.srcDirs(
        engineRoot.resolve("ui-api/src/main/java"),
        engineRoot.resolve("ui-core/src/main/java"),
        engineRoot.resolve("ui-components/src/main/java"),
        engineRoot.resolve("platform-api/src/main/java"),
        engineRoot.resolve("ui-render-opengl/src/main/java"),
        if (platform.mcVersion < 11700) engineRoot.resolve("platform-1.12.2-forge/src/main/java")
        else engineRoot.resolve("platform-1.20.1-common/src/main/java")
    )
    resources.srcDir(engineRoot.resolve("ui-render-opengl/src/main/resources"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    if (platform.mcVersion < 11300) exclude("META-INF/mods.toml") else exclude("mcmod.info")
}

val dist by tasks.registering(Copy::class) {
    from(tasks.named(if (platform.mcVersion >= 260100) "jar" else "remapJar"))
    into(rootProject.layout.buildDirectory.dir("dist"))
}
