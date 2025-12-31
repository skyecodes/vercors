plugins {
    id("app.vercors.jvm.compose")
}

dependencies {
    api(projects.core.domain)
    api(libs.composables.composeunstyled.theming)
    implementation(libs.composables.composeunstyled)
    implementation(libs.composables.ripple)
    implementation(libs.composables.icons.lucide)
}


compose.resources {
    packageOfResClass = "app.vercors.core.ui"
    nameOfResClass = "CoreRes"
    publicResClass = true
}
