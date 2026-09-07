package app.vercors

plugins {
    id("app.vercors.base")
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    macosArm64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val posixMain = create("posixMain") {
            dependsOn(getByName("commonMain"))
        }

        named("linuxMain") {
            dependsOn(posixMain)
        }

        named("macosMain") {
            dependsOn(posixMain)
        }
    }

    jvmToolchain(25)
}
