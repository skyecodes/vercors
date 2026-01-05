plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.project.domain)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.ui)
    commonMainImplementation(projects.feature.configGui.domain)
    commonMainImplementation(libs.human.readable)
    commonMainImplementation(libs.composables.icons.fontawesome.brands)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.project.ui"
    nameOfResClass = "ProjectRes"
    publicResClass = true
}
