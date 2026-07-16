import gg.essential.gradle.util.noServerRunConfigs
import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("gg.essential.defaults")
    id("gg.essential.multi-version")
}

group = "dev.kairos.ui"
version = rootProject.version

repositories {
    exclusiveContent {
        forRepository {
            ivy("https://github.com/montoyo/mcef/releases/download") {
                patternLayout { artifact("[revision]/[artifact]-[revision]-api.[ext]") }
                metadataSources { artifact() }
            }
        }
        filter { includeModule("net.montoyo", "mcef") }
    }
}

loom.noServerRunConfigs()
java.toolchain.languageVersion.set(JavaLanguageVersion.of(if (platform.mcVersion < 11700) 8 else 17))

dependencies {
    implementation(project(":engine"))
    if (platform.mcVersion < 11300) {
        compileOnly("net.montoyo:mcef:1.12.2-1.11")
    }
}

evaluationDependsOn(":engine")
val engineOutput = project(":engine").extensions.getByType<SourceSetContainer>()
    .named("main").map { it.output }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    if (platform.mcVersion < 11300) exclude("META-INF/mods.toml") else exclude("mcmod.info")
}

tasks.jar {
    from(engineOutput)
}

val dist by tasks.registering(Copy::class) {
    from(tasks.named(if (platform.mcVersion >= 260100) "jar" else "remapJar"))
    into(rootProject.layout.buildDirectory.dir("dist"))
}
