package app.vercors.jvm

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.compose.foundation)
    implementation(libs.compose.components.resources)
}
