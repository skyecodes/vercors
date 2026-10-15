plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.config.domain)
    commonMainApi(projects.feature.java.domain)
    commonMainImplementation(projects.feature.theme.domain)
    commonMainImplementation(projects.feature.project.domain)
    commonMainImplementation(projects.feature.project.ui)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.instance.ui)
    commonMainImplementation(projects.shared.data)
    commonMainImplementation(libs.filekit.dialogs.compose)
    commonMainImplementation(libs.human.readable)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.config.ui"
}
