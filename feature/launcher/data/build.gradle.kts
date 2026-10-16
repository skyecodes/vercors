plugins {
    id("app.vercors.feature.data")
}

repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    commonMainImplementation(projects.feature.launcher.domain)
    commonMainImplementation(projects.feature.account.domain)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(libs.cryptography.core)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.kzip)
    commonMainImplementation(libs.xmlutil.core)
    commonMainImplementation(libs.xmlutil.serialization)
}
