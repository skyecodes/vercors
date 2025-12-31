plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.instance.domain)
    commonMainApi(projects.feature.game.data)
}
