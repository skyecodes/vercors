package app.vercors

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("app.vercors.base")
}

// js rather than wasmJs: pedestal publishes no wasmJs artifacts
kotlin {
    js {
        // Browser tests need a headless Chrome in CI; these modules are already tested on JVM and native
        browser {
            testTask { enabled = false }
        }
    }
}

val libs = the<LibrariesForLibs>()

pluginManager.withPlugin("org.jetbrains.compose") {
    dependencies {
        add("kspJs", libs.circuit.codegen)
    }
    // Guards js UI tests bundled with webpack, and browser tests are disabled above
    tasks.matching { it.name == "checkComposeUiTestConfigurationForJs" }.configureEach { enabled = false }
}
