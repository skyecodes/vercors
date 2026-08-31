plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.home.domain)
    commonMainApi(projects.feature.project.ui)
    commonMainApi(projects.feature.instance.ui)
    commonMainImplementation(projects.feature.launcher.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.home.ui"
}
