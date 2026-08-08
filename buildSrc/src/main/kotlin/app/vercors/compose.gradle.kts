package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.base")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
    id("dev.nucleusframework")
    id("com.google.devtools.ksp")
}

kotlin {
    jvm()
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainImplementation(libs.compose.foundation)
    commonMainImplementation(libs.compose.components.resources)
    commonMainImplementation(libs.circuit.foundation)
    commonMainImplementation(libs.circuit.overlay)
    commonMainApi(libs.circuit.codegen.annotations)
    add("kspCommonMainMetadata", libs.circuit.codegen)
    add("kspJvm", libs.circuit.codegen)
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_config.conf"))
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

ksp {
    arg("circuit.codegen.mode", "metro")
}
