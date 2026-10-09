plugins {
    id("app.vercors.shared")
    kotlin("plugin.serialization")
}

dependencies {
    commonMainApi(libs.spine.api)
}
