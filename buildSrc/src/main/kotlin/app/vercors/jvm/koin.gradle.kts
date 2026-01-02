package app.vercors.jvm

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.jvm")
    id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
}

ksp {
    arg("KOIN_EXPORT_DEFINITIONS", "false")
}
