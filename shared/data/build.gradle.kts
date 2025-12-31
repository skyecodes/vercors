plugins {
    id("app.vercors.multiplatform.koin")
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainApi(projects.core.data)
    commonMainApi(projects.shared.domain)
    commonMainImplementation(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.ktor.client.content.negotiation)
    commonMainImplementation(libs.ktor.serialization.kotlinx.json)
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.appdirs)
    commonMainImplementation(libs.kfswatch)
    commonMainImplementation(libs.cryptography.core)
    jvmMainImplementation(libs.oshi)
    jvmMainImplementation(libs.ktor.client.cio)
}

kotlin {
    sourceSets.nativeMain.dependencies {
        implementation(libs.ktor.client.curl)
    }
}

buildConfig {
    packageName("app.vercors.shared.data")
    useKotlinOutput {
        internalVisibility = false
        topLevelConstants = true
    }
    buildConfigField(
        type = "String",
        name = "MICROSOFT_CLIENT_ID",
        expression = "\"${properties["microsoftClientId"] ?: System.getenv("MICROSOFT_CLIENT_ID") ?: error("AUTH_CLIENT_ID not set")}\""
    )
    buildConfigField(
        type = "String",
        name = "META_BASE_URL",
        expression = "\"${properties["metaBaseUrl"] ?: System.getenv("META_BASE_URL") ?: error("META_BASE_URL not set")}\""
    )
}
