package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
    kotlin("plugin.serialization")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainImplementation(project(":shared:data"))
    commonMainImplementation(project(project.parent!!.childProjects.getValue("domain").path))
    commonMainImplementation(libs.spine.client)
    commonMainImplementation(libs.spine.client.arrow)
}
