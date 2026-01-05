plugins {
    id("app.vercors.feature.data")
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainImplementation(projects.meta.client)
    commonMainImplementation(libs.kfswatch)
    commonMainImplementation(libs.kotlin.logging)
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.ktor.server.cio)
    commonMainImplementation(libs.cryptography.core)
    commonMainImplementation(libs.cryptography.random)
}

buildConfig {
    packageName("app.vercors.feature.account.data")
    useKotlinOutput {
        internalVisibility = false
        topLevelConstants = true
    }
    buildConfigField(
        type = "String",
        name = "MICROSOFT_CLIENT_ID",
        expression = "\"${properties["microsoftClientId"] ?: System.getenv("MICROSOFT_CLIENT_ID") ?: error("AUTH_CLIENT_ID not set")}\""
    )
}
