import io.ktor.plugin.features.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

group = "app.vercors.meta"
version = "0.1.0"

application {
    mainClass.set("app.vercors.meta.distribution.MainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.app.meta)
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_25)
        localImageName.set("skyecodes/vercors-meta")
        imageTag.set("$version")
        portMappings.set(
            listOf(
                DockerPortMapping(
                    outsideDocker = 8080,
                    insideDocker = 8080,
                    protocol = DockerPortMappingProtocol.TCP
                )
            )
        )
        externalRegistry.set(
            DockerImageRegistry.dockerHub(
                appName = provider { "vercors-meta" },
                username = providers.gradleProperty("dockerHubUsername"),
                password = providers.gradleProperty("dockerHubPassword")
            )
        )
    }
}
