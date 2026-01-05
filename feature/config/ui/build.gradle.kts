plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.config.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.config.ui"
}
