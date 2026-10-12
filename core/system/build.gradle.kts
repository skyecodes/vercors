plugins {
    id("app.vercors.shared")
    id("app.vercors.web")
    id("app.vercors.di")
    alias(libs.plugins.serialization)
}

dependencies {
    jvmMainImplementation(libs.oshi)
}
