package app.vercors.launcher

plugins {
    id("app.vercors.base")
    id("app.vercors.di")
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()
}
