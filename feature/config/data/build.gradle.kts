plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainImplementation(projects.feature.config.domain)
    jvmTestImplementation(libs.cryptography.provider.optimal)
}
