plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.account.domain)
    commonMainImplementation(libs.kfswatch)
    commonMainImplementation(projects.meta.client)
    commonMainImplementation(libs.kotlin.logging)
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.ktor.server.cio)
    commonMainImplementation(libs.cryptography.core)
    commonMainImplementation(libs.cryptography.random)
}
