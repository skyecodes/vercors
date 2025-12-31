package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.jvm.koin")
    id("app.vercors.jvm.compose")
}

val libs = the<LibrariesForLibs>()

dependencies {
    api(project(":shared:ui"))
    implementation(libs.composables.icons.lucide)
    implementation(libs.coil.compose)
}
