plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(compose.desktop.currentOs)
            implementation(libs.composables.composeunstyled)
            implementation(libs.composables.ripple)
            implementation(libs.composables.icons.lucide)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.flowmvi.core)
            implementation(libs.flowmvi.compose)
            implementation(libs.flowmvi.android)
            implementation(libs.flowmvi.metrics)
            implementation(libs.kotlin.logging)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    compilerOptions.freeCompilerArgs.addAll(
        "-Xcontext-parameters",
        "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
    )
}


compose.resources {
    packageOfResClass = "app.vercors.shared.ui.res"
}
