plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.config.domain)
    commonMainApi(projects.feature.game.domain)
    commonMainImplementation(libs.filekit.dialogs.compose)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.config.ui"
}
