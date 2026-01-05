plugins {
    id("app.vercors.compose")
}

dependencies {
    commonMainApi(projects.core.domain)
    commonMainApi(libs.composables.composeunstyled.theming)
    commonMainImplementation(libs.composables.composeunstyled)
    commonMainImplementation(libs.composables.ripple)
    commonMainImplementation(libs.composables.icons.lucide)
    commonMainImplementation(libs.androidx.lifecycle.runtime)
}

compose.resources {
    packageOfResClass = "app.vercors.core.ui"
    nameOfResClass = "CoreRes"
    publicResClass = true
}
