package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("multiplatform")
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
        "-opt-in=kotlin.uuid.ExperimentalUuidApi"
    )
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonTestImplementation(libs.kotlin.test)
}
