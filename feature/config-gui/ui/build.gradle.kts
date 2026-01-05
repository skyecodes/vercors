plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.configGui.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.config.gui.ui"
}
