plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildconfig)
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
            implementation(projects.shared.domain)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlin.logging)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.cryptography.core)
            implementation(libs.cryptography.random)
            implementation(libs.cryptography.provider.optimal)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.logback)
        }
    }
    compilerOptions.freeCompilerArgs.addAll(
        "-Xannotation-default-target=param-property",
        "-opt-in=kotlin.time.ExperimentalTime",
        "-opt-in=kotlin.uuid.ExperimentalUuidApi",
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

buildConfig {
    packageName("app.vercors.launcher.common.domain")
    useKotlinOutput {
        internalVisibility = false
        topLevelConstants = true
    }
    buildConfigField("String", "LAUNCHER_VERSION", "\"${project.version}\"")
}
