package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.multiplatform.koin")
    kotlin("plugin.serialization")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(project(":shared:data"))
    commonMainImplementation(libs.kotlin.logging)
}
