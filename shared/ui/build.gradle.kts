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
    commonMainApi(libs.metrox.viewmodel.compose)
}

compose.resources {
    packageOfResClass = "app.vercors.shared.ui"
    nameOfResClass = "SharedRes"
    publicResClass = true
}
