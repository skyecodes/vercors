package app.vercors

plugins {
    id("app.vercors.base")
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()
}
