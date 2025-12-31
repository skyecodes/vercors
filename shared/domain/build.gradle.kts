plugins {
    id("app.vercors.multiplatform.koin")
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainApi(projects.core.domain)
}

buildConfig {
    packageName("app.vercors.shared.domain")
    useKotlinOutput {
        internalVisibility = false
        topLevelConstants = true
    }
    buildConfigField(
        type = "String",
        name = "LAUNCHER_NAME",
        expression = """"vercors""""
    )
    buildConfigField(
        type = "String",
        name = "LAUNCHER_VERSION",
        expression = """"$version""""
    )
}
