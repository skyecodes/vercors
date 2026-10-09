plugins {
    id("app.vercors.backend")
}

dependencies {
    commonMainApi(libs.spine.server)
    commonMainApi(libs.spine.server.arrow)
    commonTestImplementation(libs.ktor.server.test.host)
}
