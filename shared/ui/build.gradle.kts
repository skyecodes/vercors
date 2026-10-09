plugins {
    id("app.vercors.compose")
    id("app.vercors.di")
}

dependencies {
    commonMainApi(projects.core.ui)
    commonMainApi(libs.androidx.lifecycle.viewmodel)
    commonMainApi(libs.androidx.lifecycle.runtime)
    commonMainApi(libs.androidx.navigation3.ui)
    commonMainApi(libs.composables.icons.lucide)
    commonMainApi(libs.composables.icons.fontawesome.brands)
    commonMainApi(libs.metrox.viewmodel.compose)
    commonMainImplementation(libs.filekit.dialogs.compose)
}

compose.resources {
    packageOfResClass = "app.vercors.shared.ui"
    nameOfResClass = "SharedRes"
    publicResClass = true
}
