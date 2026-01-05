plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktorfit)
}

dependencies {
    commonMainApi(projects.meta.lib)
    commonMainImplementation(libs.ktorfit.lib)
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}
