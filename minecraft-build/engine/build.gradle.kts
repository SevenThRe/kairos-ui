plugins {
    `java-library`
}

group = "dev.kairos.ui"
version = rootProject.version

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

val engineRoot = rootProject.projectDir.parentFile
sourceSets.main {
    java.srcDirs(
        engineRoot.resolve("ui-api/src/main/java"),
        engineRoot.resolve("ui-core/src/main/java"),
        engineRoot.resolve("ui-components/src/main/java"),
        engineRoot.resolve("platform-api/src/main/java"),
        engineRoot.resolve("ui-render-opengl/src/main/java"),
        engineRoot.resolve("platform-1.12.2-forge/src/main/java"),
        engineRoot.resolve("platform-1.20.1-common/src/main/java")
    )
    resources.srcDir(engineRoot.resolve("ui-render-opengl/src/main/resources"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
