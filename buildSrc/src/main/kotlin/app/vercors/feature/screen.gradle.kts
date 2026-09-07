package app.vercors.feature

plugins {
    id("app.vercors.base")
}

kotlin {
    jvm()
}

dependencies {
    commonMainApi(project(":shared:ui:screen"))
    commonMainApi(project(project.parent!!.parent!!.childProjects.getValue("domain").path))
}
