plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
}

dependencies {
    commonMainApi(libs.kotlinx.io.core)
    commonMainImplementation(libs.cryptography.core)
}
