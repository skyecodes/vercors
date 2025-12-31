plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.bundles.gradle.plugins)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
