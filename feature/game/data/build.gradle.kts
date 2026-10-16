plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.api)
    commonMainImplementation(libs.kzip)
}
