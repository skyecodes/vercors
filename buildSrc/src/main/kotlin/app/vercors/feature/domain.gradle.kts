package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.multiplatform.koin")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(project(":shared:domain"))
    commonMainImplementation(libs.kotlin.logging)
}
