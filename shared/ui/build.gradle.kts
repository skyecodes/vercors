plugins {
    id("app.vercors.jvm.koin")
    id("app.vercors.jvm.compose")
}

dependencies {
    api(projects.core.ui)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.composables.icons.lucide)
    api(libs.flowmvi.core)
    api(libs.flowmvi.compose)
    api(libs.flowmvi.android)
    api(libs.kotlin.logging)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.navigation3)
}

compose.resources {
    packageOfResClass = "app.vercors.shared.ui"
    nameOfResClass = "SharedRes"
    publicResClass = true
}
