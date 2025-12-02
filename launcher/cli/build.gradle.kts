import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.launcher.common.domain)
            implementation(projects.launcher.common.data)
            implementation(projects.shared.domain)
            implementation(libs.clikt)
            implementation(libs.mordant.coroutines)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.cio)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
        commonMain.configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.logback)
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

    compilerOptions.freeCompilerArgs.addAll(
        "-opt-in=kotlin.time.ExperimentalTime",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-opt-in=kotlin.uuid.ExperimentalUuidApi"
    )
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspJvm", libs.koin.ksp.compiler)
    add("kspLinuxX64", libs.koin.ksp.compiler)
    add("kspLinuxArm64", libs.koin.ksp.compiler)
    add("kspMacosX64", libs.koin.ksp.compiler)
    add("kspMacosArm64", libs.koin.ksp.compiler)
    add("kspMingwX64", libs.koin.ksp.compiler)
}

tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}
