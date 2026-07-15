pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://repo.essential.gg/public")
        maven("https://maven.minecraftforge.net")
    }
    plugins {
        val egtVersion = "0.7.2"
        id("gg.essential.multi-version.root") version egtVersion
        id("gg.essential.multi-version") version egtVersion
        id("gg.essential.defaults") version egtVersion
    }
}

rootProject.name = "kairos-ui-minecraft"

listOf("1.12.2-forge", "1.20.1-forge").forEach { target ->
    include(":$target")
    project(":$target").apply {
        projectDir = file("versions/$target")
        buildFileName = "../../build.gradle.kts"
    }
}

rootProject.buildFileName = "root.gradle.kts"
