plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.project.domain)
    implementation(projects.feature.game.domain)
    implementation(projects.feature.game.ui)
    implementation(projects.feature.configGui.domain)
    implementation(libs.human.readable)
    implementation(libs.composables.icons.fontawesome.brands)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.project.ui"
    nameOfResClass = "ProjectRes"
    publicResClass = true
}
