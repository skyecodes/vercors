package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("multiplatform")
    id("dev.mokkery")
}

base.archivesName.set(project.path.drop(1).replace(":", "-"))
version = "0.1.0"

repositories {
    mavenCentral()
    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
}

kotlin {
    compilerOptions.freeCompilerArgs.addAll(
        "-Xexpect-actual-classes",
    )
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainApi(libs.kotlinx.coroutines.core)
    commonMainApi(libs.arrow.core)
    commonMainApi(libs.arrow.fx.coroutines)
    commonMainApi(libs.pedestal.state)
    commonMainApi(libs.pedestal.state.coroutines)
    commonTestApi(libs.kotlin.test)
    commonTestApi(libs.kotlinx.coroutines.test)
    commonTestApi(libs.turbine)
    commonMainImplementation(libs.kotlin.logging)
}
