plugins {
    id("app.vercors.feature.data")
}

repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    commonMainApi(projects.feature.launcher.domain)
    commonMainImplementation(libs.cryptography.core)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.kzip)
    commonMainImplementation(libs.xmlutil.core)
    commonMainImplementation(libs.xmlutil.serialization)
}
