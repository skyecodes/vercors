plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.launcher.domain)
    commonMainImplementation(libs.cryptography.core)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.kzip)
}
