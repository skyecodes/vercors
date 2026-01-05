import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("app.vercors.compose")
    id("app.vercors.di")
    alias(libs.plugins.compose.hot.reload)
}

dependencies {
    commonMainImplementation(projects.shared.domain)
    commonMainImplementation(projects.shared.data)
    commonMainImplementation(projects.shared.ui)
    commonMainImplementation(projects.feature.account.domain)
    commonMainImplementation(projects.feature.account.data)
    commonMainImplementation(projects.feature.account.ui)
    commonMainImplementation(projects.feature.config.domain)
    commonMainImplementation(projects.feature.config.data)
    commonMainImplementation(projects.feature.config.ui)
    commonMainImplementation(projects.feature.configGui.domain)
    commonMainImplementation(projects.feature.configGui.data)
    commonMainImplementation(projects.feature.configGui.ui)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.data)
    commonMainImplementation(projects.feature.game.ui)
    commonMainImplementation(projects.feature.home.domain)
    commonMainImplementation(projects.feature.home.data)
    commonMainImplementation(projects.feature.home.ui)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.instance.data)
    commonMainImplementation(projects.feature.instance.ui)
    commonMainImplementation(projects.feature.launcher.domain)
    commonMainImplementation(projects.feature.launcher.data)
    commonMainImplementation(projects.feature.launcher.ui)
    commonMainImplementation(projects.feature.project.domain)
    commonMainImplementation(projects.feature.project.data)
    commonMainImplementation(projects.feature.project.ui)
    commonMainImplementation(projects.meta.client)
    commonMainImplementation(compose.desktop.currentOs)
    commonMainImplementation(libs.coil.compose)
    commonMainImplementation(libs.cryptography.provider.optimal)
    commonMainImplementation(libs.kotlinx.coroutines.swing)
    commonMainImplementation(libs.logback)
    commonMainImplementation(libs.coil.network.ktor3)
    commonMainImplementation(libs.kotlinx.io.core)
    commonMainImplementation(libs.kotlin.logging)
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
    packageOfResClass = "app.vercors.launcher.gui"
}
