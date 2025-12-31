plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.game.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.game.ui"
    nameOfResClass = "GameRes"
    publicResClass = true
}
