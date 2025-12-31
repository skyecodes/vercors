plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.game.domain)
    commonMainImplementation(libs.kfswatch)
    commonMainImplementation(projects.meta.client)
    commonMainImplementation(libs.kotlin.logging)
    commonMainImplementation(libs.ktorfit.lib)
}
