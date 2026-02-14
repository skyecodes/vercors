plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
}

dependencies {
    commonMainApi(libs.kotlinx.io.core)
    commonMainApi(libs.spine.client)
    commonMainImplementation(libs.spine.client.arrow)
    commonMainImplementation(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.ktor.client.content.negotiation)
    commonMainImplementation(libs.ktor.serialization.kotlinx.json)
    jvmMainImplementation(libs.ktor.client.cio)
}

kotlin {
    sourceSets.nativeMain.dependencies {
        implementation(libs.ktor.client.curl)
    }
}
