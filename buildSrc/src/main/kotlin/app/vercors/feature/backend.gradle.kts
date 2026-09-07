package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.backend")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainImplementation(project(":shared:backend"))
    commonMainImplementation(project(project.parent!!.childProjects.getValue("api").path))
    commonMainImplementation(libs.spine.server)
    commonMainImplementation(libs.spine.server.arrow)
}
