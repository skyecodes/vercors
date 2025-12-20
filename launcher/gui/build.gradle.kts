import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

repositories {
    mavenCentral()
    google()
    maven("https://jogamp.org/deployment/maven")
}

dependencies {
    implementation(projects.launcher.common.domain)
    implementation(projects.launcher.common.data)
    implementation(projects.shared.domain)
    implementation(projects.shared.data)
    implementation(projects.shared.ui)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.datetime)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.navigation3)
    implementation(libs.koin.annotations)
    implementation(libs.logback)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.composables.composeunstyled)
    implementation(libs.composables.ripple)
    implementation(libs.composables.icons.lucide)
    implementation(libs.flowmvi.core)
    implementation(libs.flowmvi.compose)
    implementation(libs.flowmvi.android)
    implementation(libs.flowmvi.metrics)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.markdown.renderer)
    implementation(libs.markdown.renderer.coil3)
    api(libs.webview)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.io.core)
    implementation(libs.appdirs)
    implementation(libs.kfswatch)
    implementation(libs.ktor.client.cio)
    implementation(libs.human.readable)
    implementation(libs.kotlin.logging)
    ksp(libs.koin.ksp.compiler)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.flowmvi.test)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=org.koin.core.annotation.KoinExperimentalAPI"
        )
    }
}

compose.desktop {
    application {
        mainClass = "app.vercors.launcher.gui.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "app.vercors.launcher"
            packageVersion = "1.0.0"
        }
    }
}

compose.resources {
    packageOfResClass = "app.vercors.launcher.gui.res"
}
