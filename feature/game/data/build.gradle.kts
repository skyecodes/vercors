plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.game.domain)
    commonMainApi(projects.feature.game.api)
}
