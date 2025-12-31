package app.vercors.multiplatform

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.multiplatform")
    id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()

dependencies {
    commonMainImplementation(project.dependencies.platform(libs.koin.bom))
    commonMainImplementation(libs.koin.core)
    commonMainImplementation(libs.koin.annotations)
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
