plugins {
    id("app.vercors.shared")
}

dependencies {
    commonMainApi(libs.kotlinx.coroutines.core)
    commonTestApi(libs.kotlinx.coroutines.test)
}
