plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.launcher.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.launcher.ui"
}
