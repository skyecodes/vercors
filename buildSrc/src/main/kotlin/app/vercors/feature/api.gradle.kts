package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.shared")
    kotlin("plugin.serialization")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(project(":shared:api"))
    commonMainApi(libs.spine.api)
}
