plugins {
    id("app.vercors.feature.data")
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainImplementation(projects.core.system)
    commonMainImplementation(projects.core.filesystem)
    commonMainImplementation(libs.ktor.server.cio)
}

buildConfig {
    packageName("app.vercors.feature.account.data")
    useKotlinOutput {
        topLevelConstants = true
    }
    buildConfigField(
        type = "String",
        name = "MICROSOFT_CLIENT_ID",
        expression = "\"${properties["microsoftClientId"] ?: System.getenv("MICROSOFT_CLIENT_ID") ?: error("MICROSOFT_CLIENT_ID not set")}\""
    )
}
