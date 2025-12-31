package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("jvm")
}

val libs = the<LibrariesForLibs>()

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

dependencies {
    implementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(21)

    compilerOptions.freeCompilerArgs.addAll(
        "-Xannotation-default-target=param-property",
        "-Xcontext-parameters",
        "-opt-in=kotlin.uuid.ExperimentalUuidApi"
    )
}
