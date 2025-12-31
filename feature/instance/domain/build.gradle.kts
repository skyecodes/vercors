plugins {
    id("app.vercors.feature.domain")
}

dependencies {
    commonMainApi(projects.feature.game.domain)
}
