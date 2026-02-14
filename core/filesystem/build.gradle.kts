plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
    alias(libs.plugins.serialization)
}

dependencies {
    commonMainApi(libs.kotlinx.serialization.json)
    commonMainApi(libs.kotlinx.io.core)
    commonMainApi(libs.ktor.client.core)
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(libs.appdirs)
    commonMainImplementation(libs.kfswatch)
}
