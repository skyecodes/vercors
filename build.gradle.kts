plugins {
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.graalvm) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.mokkery) apply false
}

group = property("appGroup") as String
version = property("appVersion") as String
