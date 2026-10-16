plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainImplementation(projects.feature.project.domain)
    commonMainImplementation(projects.feature.project.api)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(libs.kzip)
}
