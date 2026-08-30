import dev.nucleusframework.desktop.application.dsl.AppImageCategory
import dev.nucleusframework.desktop.application.dsl.CompressionLevel
import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    id("app.vercors.compose")
    id("app.vercors.di")
    alias(libs.plugins.compose.hot.reload)
}

dependencies {
    commonMainImplementation(projects.shared.domain)
    commonMainImplementation(projects.shared.data)
    commonMainImplementation(projects.shared.ui)
    commonMainImplementation(projects.core.cryptography)
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(projects.core.filesystem)
    commonMainImplementation(projects.core.network)
    commonMainImplementation(projects.core.system)
    commonMainImplementation(projects.core.ui)
    commonMainImplementation(projects.feature.account.domain)
    commonMainImplementation(projects.feature.account.data)
    commonMainImplementation(projects.feature.account.ui)
    commonMainImplementation(projects.feature.config.domain)
    commonMainImplementation(projects.feature.config.data)
    commonMainImplementation(projects.feature.config.ui)
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
    commonMainImplementation(projects.feature.theme.domain)
    commonMainImplementation(projects.feature.theme.data)
    commonMainImplementation(compose.desktop.currentOs)
    commonMainImplementation(libs.nucleus.application)
    commonMainImplementation(libs.nucleus.decorated.window.tao)
    commonMainImplementation(libs.nucleus.notification.common)
    commonMainImplementation(libs.nucleus.taskbar.progress.tao)
    commonMainImplementation(libs.nucleus.composenativetray)
    commonMainImplementation(libs.coil.compose)
    commonMainImplementation(libs.coil.network.ktor3)
    //commonMainImplementation(libs.coil.gif)
    commonMainImplementation(libs.cryptography.provider.optimal)
    commonMainImplementation(libs.logback)
    commonMainImplementation(libs.kotlinx.io.core)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

nucleus.application {
    mainClass = "app.vercors.launcher.gui.MainKt"

    buildTypes.release.proguard {
        isEnabled = true
        configurationFiles.from("proguard-rules.pro")
    }

    nativeDistributions {
        targetFormats(*TargetFormat.entries.toTypedArray())
        modules("jdk.security.auth")
        appName = property("appName") as String
        packageName = property("appId") as String
        packageVersion = property("appVersion") as String
        description = "A modern Minecraft Launcher with mod support and much more!"
        vendor = property("appVendor") as String
        homepage = "https://vercors.app"
        enableAotCache = true

        linux {
            shortcut = true
            appCategory = "Game"
            menuGroup = "Games"
            debMaintainer = "skyecodes <contact@skye.codes>"
            rpmLicenseType = "MIT"
            startupWMClass = "vercors"
            compressionLevel = CompressionLevel.Ultra

            appImage {
                category = AppImageCategory.Game
            }

            snap {
            }

            flatpak {
                runtimeVersion = "25.08"
            }
        }
    }
}

compose.resources {
    packageOfResClass = "app.vercors.launcher.gui"
}
