plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.configGui.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.config.gui.ui"
}
