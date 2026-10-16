plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.core.cryptography)
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(projects.core.system)
    commonMainImplementation(projects.core.nbt)
    commonMainImplementation(libs.kzip)
    commonMainImplementation(libs.ktoml.core)
    jvmTestImplementation(libs.cryptography.provider.optimal)
}
