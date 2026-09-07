plugins {
    id("app.vercors.backend")
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

dependencies {
    commonMainApi(project(":feature:project:backend"))
    commonMainImplementation(project(":shared:backend"))
    commonMainImplementation(project(":feature:project:api"))
    commonMainImplementation(libs.spine.server)
    commonMainImplementation(libs.spine.server.arrow)
    commonMainImplementation(libs.ktorfit.lib)
    commonMainImplementation(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.flexmark.html2md.converter)
}
