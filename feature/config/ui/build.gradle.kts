plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.config.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.config.ui"
}
