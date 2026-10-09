import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("app.vercors.cli")
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonMainImplementation(projects.core.filesystem)
    commonMainImplementation(projects.shared.command)
    commonMainImplementation(projects.shared.data)
    commonMainImplementation(projects.shared.domain)
    commonMainImplementation(projects.feature.account.command)
    commonMainImplementation(projects.feature.account.domain)
    commonMainImplementation(projects.feature.account.data)
    commonMainImplementation(projects.feature.config.command)
    commonMainImplementation(projects.feature.config.domain)
    commonMainImplementation(projects.feature.config.data)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.data)
    commonMainImplementation(projects.feature.java.command)
    commonMainImplementation(projects.feature.java.domain)
    commonMainImplementation(projects.feature.java.data)
    commonMainImplementation(projects.feature.instance.command)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.instance.data)
    commonMainImplementation(projects.feature.launcher.command)
    commonMainImplementation(projects.feature.launcher.domain)
    commonMainImplementation(projects.feature.launcher.data)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.cryptography.provider.optimal)
    jvmMainImplementation(libs.logback)
    nativeMainImplementation(libs.kotlin.logging)
}

kotlin {
    sourceSets {
        commonMain.configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable {
                entryPoint = "app.vercors.launcher.cli.main"
                baseName = "vercors"
                linkerOpts += "-Wl,--allow-multiple-definition"
            }
        }
    }
}

buildConfig {
    packageName("app.vercors.launcher.cli")
    buildConfigField("String", "APP_ID", "\"${property("appId")}\"")
    buildConfigField("String", "APP_NAME", "\"${property("appName")}\"")
    buildConfigField("String", "APP_VENDOR", "\"${property("appVendor")}\"")
    buildConfigField("String", "APP_VERSION", "\"${property("appVersion")}\"")
}
