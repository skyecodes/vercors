plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
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
            implementation(projects.meta.lib)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktorfit.lib)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
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
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    )
}

