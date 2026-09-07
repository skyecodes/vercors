package app.vercors.feature

import org.gradle.accessors.dm.LibrariesForLibs
import java.util.Locale

plugins {
    id("app.vercors.compose")
    id("app.vercors.di")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainImplementation(project(":shared:ui"))
    commonMainImplementation(project(project.parent!!.childProjects.getValue("domain").path))
    commonMainApi(project(project.childProjects.getValue("screen").path))
    commonMainImplementation(libs.composables.icons.lucide)
    commonMainImplementation(libs.coil.compose)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.${project.parent!!.name}.ui"
    nameOfResClass = project.parent!!.name.split("-").joinToString("") {
        it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } + "Res"
    publicResClass = true
}
