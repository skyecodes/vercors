plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    application
}

group = "app.vercors.meta"
version = "1.0.0"
application {
    mainClass.set("app.vercors.meta.backend.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.meta.lib)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.simple.cache)
    implementation(libs.ktor.simple.redis.cache)
    implementation(libs.ktor.simple.memory.cache)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktorfit.lib)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.annotations)

    ksp(libs.koin.ksp.compiler)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    jvmToolchain(25)
    compilerOptions.freeCompilerArgs.addAll(
        "-Xannotation-default-target=param-property",
        "-opt-in=kotlin.time.ExperimentalTime",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    )
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_25)
        localImageName.set("vercors/meta-backend")
        imageTag.set("$version")
    }
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}
