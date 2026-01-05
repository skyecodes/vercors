package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.base")
    id("app.vercors.di")
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainImplementation(project(":shared:command"))
    commonMainImplementation(project.parent!!.childProjects.getValue("domain"))
    commonMainApi(libs.clikt)
    commonMainImplementation(libs.mordant.coroutines)
}
