plugins {
    id("app.vercors.base")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "vercors-demo.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(projects.app.launcherGui.shell)
            implementation(projects.core.ui)
            implementation(projects.shared.ui)
            implementation(projects.feature.account.ui)
            implementation(projects.feature.game.ui)
            implementation(projects.feature.home.ui)
            implementation(projects.feature.instance.ui)
            implementation(projects.feature.project.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.components.resources)
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.overlay)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.ktor.client.js)
        }
    }
}

compose.resources {
    packageOfResClass = "app.vercors.launcher.demo"
    nameOfResClass = "DemoRes"
    generateResClass = always
}
