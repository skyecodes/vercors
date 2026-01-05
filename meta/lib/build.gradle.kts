plugins {
    id("app.vercors.shared")
    alias(libs.plugins.serialization)
}

dependencies {
    commonMainApi(projects.shared.domain)
    commonMainApi(libs.kotlinx.serialization.core)
    commonMainImplementation(libs.kotlinx.serialization.json)
}
