plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.home.domain)
    api(projects.feature.project.ui)
    api(projects.feature.instance.ui)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.home.ui"
}
