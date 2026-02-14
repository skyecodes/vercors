plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainApi(projects.core.domain)
    commonMainApi(projects.core.system)
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
