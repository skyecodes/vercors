package app.vercors.feature

plugins {
    id("app.vercors.cli")
}

dependencies {
    commonMainImplementation(project(":shared:command"))
    commonMainImplementation(project(project.parent!!.childProjects.getValue("domain").path))
}
