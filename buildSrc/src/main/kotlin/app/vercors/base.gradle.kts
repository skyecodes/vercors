package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("multiplatform")
    id("dev.mokkery")
}

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
        "-Xcontext-parameters",
        "-Xannotation-default-target=param-property",
        "-Xexpect-actual-classes",
        "-Xexplicit-backing-fields",
        "-opt-in=kotlin.uuid.ExperimentalUuidApi"
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
