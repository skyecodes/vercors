plugins {
    id("app.vercors.shared")
    alias(libs.plugins.serialization)
}

dependencies {
    commonMainApi(projects.core.domain)
    commonMainApi(libs.kotlinx.serialization.json)
    commonMainApi(libs.kotlinx.io.core)
    commonMainImplementation(libs.kfswatch)
}
