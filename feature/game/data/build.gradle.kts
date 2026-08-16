plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.game.domain)
    commonMainApi(projects.feature.game.api)
    commonMainImplementation(libs.kzip)
    jvmMainImplementation(libs.jna)
    jvmMainImplementation(libs.jna.platform)
}
