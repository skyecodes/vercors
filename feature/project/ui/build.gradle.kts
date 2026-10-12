plugins {
    id("app.vercors.feature.ui")
    id("app.vercors.web")
}

dependencies {
    commonMainApi(projects.feature.project.domain)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.ui)
    commonMainImplementation(projects.feature.config.domain)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.instance.ui)
    commonMainImplementation(libs.human.readable)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.project.ui"
    nameOfResClass = "ProjectRes"
    publicResClass = true
}
