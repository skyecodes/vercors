plugins {
    id("app.vercors.feature.domain")
}

dependencies {
    commonMainApi(projects.feature.game.domain)
    commonMainApi(projects.feature.instance.domain)
}
