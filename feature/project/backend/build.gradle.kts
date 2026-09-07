plugins {
    id("app.vercors.feature.backend")
}

dependencies {
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.kotlinx.serialization.json)
}
