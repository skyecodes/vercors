plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.serialization)
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
            implementation(projects.launcher.common.domain)
            implementation(projects.shared.data)
            implementation(projects.shared.domain)
            implementation(projects.meta.lib)
            implementation(projects.meta.client)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlin.logging)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktorfit.lib)
            implementation(libs.kfswatch)
            implementation(libs.appdirs)
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
            implementation(libs.turbine)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.logback)
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.curl)
        }
    }
    compilerOptions.freeCompilerArgs.addAll(
        "-Xannotation-default-target=param-property",
        "-opt-in=kotlin.time.ExperimentalTime",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
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

buildConfig {
    packageName("app.vercors.launcher.common.data")
    useKotlinOutput {
        internalVisibility = false
        topLevelConstants = true
    }
    buildConfigField(
        "String",
        "MICROSOFT_CLIENT_ID",
        "\"${properties["microsoftClientId"] ?: System.getenv("MICROSOFT_CLIENT_ID") ?: error("AUTH_CLIENT_ID not set")}\""
    )
}
