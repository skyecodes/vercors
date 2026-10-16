plugins {
    id("app.vercors.feature.ui")
    id("app.vercors.web")
}

dependencies {
    commonMainApi(projects.feature.home.domain)
    commonMainApi(projects.feature.project.ui)
    commonMainApi(projects.feature.instance.ui)
    commonMainImplementation(projects.feature.project.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.home.ui"
}
