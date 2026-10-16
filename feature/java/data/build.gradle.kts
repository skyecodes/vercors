plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainImplementation(projects.feature.java.domain)
    commonMainImplementation(projects.feature.game.api)
    commonMainImplementation(projects.feature.config.domain)
    jvmMainImplementation(libs.jna)
    jvmMainImplementation(libs.jna.platform)
}
