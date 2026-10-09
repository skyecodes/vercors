plugins {
    id("app.vercors.backend")
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

dependencies {
    commonMainApi(projects.feature.project.backend)
    commonMainImplementation(projects.shared.backend)
    commonMainImplementation(projects.feature.project.api)
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.kotlinx.serialization.json)
}
