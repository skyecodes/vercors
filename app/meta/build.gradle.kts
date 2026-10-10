plugins {
    id("app.vercors.backend")
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainImplementation(projects.feature.account.backend)
    commonMainImplementation(projects.feature.game.backend)
    commonMainImplementation(projects.feature.project.backend)
    commonMainImplementation(projects.feature.project.backend.curseforge)
    commonMainImplementation(projects.feature.project.backend.modrinth)
    commonMainImplementation(projects.shared.backend)

    commonMainImplementation(libs.ktor.server.cio)
    commonMainImplementation(libs.ktor.server.content.negotiation)
    commonMainImplementation(libs.ktor.server.status.pages)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.ktor.client.cio)
    commonMainImplementation(libs.ktor.client.content.negotiation)
    commonMainImplementation(libs.ktor.serialization.kotlinx.json)
    commonMainImplementation(libs.ktorfit.lib)

    commonTestImplementation(libs.ktor.server.test.host)

    jvmMainImplementation(libs.logback)
}

buildConfig {
    packageName("app.vercors.meta")
    buildConfigField("String", "APP_VERSION", "\"${property("appVersion")}\"")
}
