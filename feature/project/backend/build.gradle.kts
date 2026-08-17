plugins {
    id("app.vercors.feature.backend")
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

dependencies {
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.flexmark.html2md.converter)
}
