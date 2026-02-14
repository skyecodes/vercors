plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graalvm)
    application
}

dependencies {
    implementation(projects.app.launcherCli)
}

application {
    mainClass = "app.vercors.launcher.cli.MainKt"
}

graalvmNative {
    binaries {
        all {
            buildArgs.add("--exact-reachability-metadata")
            runtimeArgs.add("-XX:MissingRegistrationReportingMode=Warn")
        }

        named("main") {
            mainClass = "app.vercors.launcher.cli.MainKt"
            imageName = "vercors-cli"
            resources.includedPatterns.add("logback.xml")
        }
    }

    agent {
        enabled = true
        metadataCopy {
            inputTaskNames.add("run")
            outputDirectories.add("src/main/resources/META-INF/native-image/app.vercors/graalvm/")
            mergeWithExisting = true
        }
    }
}
