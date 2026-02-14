package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.plugins

plugins {
    id("app.vercors.base")
    id("app.vercors.di")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    jvmToolchain(25)
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(libs.ktor.server.core)
}
