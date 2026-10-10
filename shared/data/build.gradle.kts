plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
    alias(libs.plugins.serialization)
}

dependencies {
    commonMainApi(projects.core.filesystem)
    commonMainApi(projects.core.network)
    commonMainApi(projects.core.cryptography)
    commonMainApi(projects.shared.domain)
    commonMainImplementation(libs.arrow.resilience)
    commonMainImplementation(libs.kzip)
    commonMainImplementation(libs.korlibs.compression)
}
