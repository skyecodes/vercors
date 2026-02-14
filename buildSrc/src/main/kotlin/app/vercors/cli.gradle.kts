package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(libs.clikt)
    commonMainApi(libs.mordant.coroutines)
}
