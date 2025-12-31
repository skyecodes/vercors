plugins {
    id("app.vercors.multiplatform")
}

dependencies {
    commonMainApi(libs.kotlinx.coroutines.core)
    commonTestApi(libs.kotlinx.coroutines.test)
}
