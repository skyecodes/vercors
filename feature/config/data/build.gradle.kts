plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.config.domain)
    jvmTestImplementation(libs.cryptography.provider.optimal)
}
