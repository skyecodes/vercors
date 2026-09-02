plugins {
    id("app.vercors.backend")
}

dependencies {
    commonMainApi(libs.spine.server)
    commonMainApi(libs.spine.server.arrow)
    commonMainImplementation(libs.ktor.server.caching.headers)
    commonTestImplementation(libs.ktor.server.test.host)
}
