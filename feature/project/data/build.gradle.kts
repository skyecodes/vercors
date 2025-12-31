plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.project.domain)
    commonMainImplementation(projects.meta.client)
    commonMainImplementation(libs.kotlin.logging)
    commonMainImplementation(libs.ktorfit.lib)
}
