plugins {
    id("app.vercors.backend")
}

dependencies {
    commonMainApi(libs.spine.server)
    commonMainApi(libs.spine.server.arrow)
    commonMainApi(libs.kotlinx.serialization.json)
    commonTestImplementation(libs.ktor.server.test.host)
}
