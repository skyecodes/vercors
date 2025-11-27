plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
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
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
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

