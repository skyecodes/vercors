plugins {
    id("app.vercors.multiplatform")
    alias(libs.plugins.serialization)
}

dependencies {
    commonMainApi(projects.shared.domain)
    commonMainApi(libs.kotlinx.serialization.core)
    commonMainImplementation(libs.kotlinx.serialization.json)
}
