plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.java.domain)
    commonMainApi(projects.feature.game.api)
    jvmMainImplementation(libs.jna)
    jvmMainImplementation(libs.jna.platform)
}
