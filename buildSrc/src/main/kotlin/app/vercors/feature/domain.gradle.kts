package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(project(":shared:domain"))
}
