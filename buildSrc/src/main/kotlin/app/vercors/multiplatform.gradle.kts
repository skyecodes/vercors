package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("multiplatform")
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
    commonTestImplementation(libs.kotlin.test)
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()

    jvmToolchain(21)

    compilerOptions.freeCompilerArgs.addAll(
        "-Xannotation-default-target=param-property",
        "-Xcontext-parameters",
        "-opt-in=kotlin.uuid.ExperimentalUuidApi"
    )
}
