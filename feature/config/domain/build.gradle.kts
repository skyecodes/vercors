plugins {
    id("app.vercors.feature.domain")
}

kotlin {
    sourceSets {
        // runBlocking doesn't exist on js
        val blockingMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain { dependsOn(blockingMain) }
        nativeMain { dependsOn(blockingMain) }
    }
}
