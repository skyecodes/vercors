plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.instance.domain)
    commonMainApi(projects.feature.game.data)
    commonMainApi(projects.core.cryptography)
    commonMainApi(projects.core.domain)
    commonMainImplementation(projects.core.system)
    commonMainImplementation(projects.core.nbt)
    commonMainImplementation(libs.kzip)
    commonMainImplementation(libs.ktoml.core)
}
