import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("app.vercors.launcher.cli")
}

dependencies {
    commonMainImplementation(projects.shared.data)
    commonMainImplementation(projects.shared.domain)
    commonMainImplementation(projects.feature.account.domain)
    commonMainImplementation(projects.feature.account.data)
    commonMainImplementation(projects.feature.config.domain)
    commonMainImplementation(projects.feature.config.data)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.data)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.instance.data)
    commonMainImplementation(projects.feature.launcher.domain)
    commonMainImplementation(projects.feature.launcher.data)
    commonMainImplementation(projects.feature.project.domain)
    commonMainImplementation(projects.feature.project.data)
    commonMainImplementation(projects.meta.client)
    commonMainImplementation(libs.clikt)
    commonMainImplementation(libs.mordant.coroutines)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.cryptography.provider.optimal)
    jvmMainImplementation(libs.logback)
}

kotlin {
    sourceSets {
        commonMain.configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }

        nativeMain.dependencies {
            implementation(libs.kotlin.logging)
        }
    }

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable {
                entryPoint = "app.vercors.launcher.cli.main"
                baseName = "vercors-cli"
            }
        }
    }
}
