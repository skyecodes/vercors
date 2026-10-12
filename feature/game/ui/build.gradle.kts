plugins {
    id("app.vercors.feature.ui")
    id("app.vercors.web")
}

compose.resources {
    packageOfResClass = "app.vercors.feature.game.ui"
    nameOfResClass = "GameRes"
    publicResClass = true
}
