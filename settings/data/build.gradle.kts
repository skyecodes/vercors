plugins {
    id("app.vercors.launcher.data")
}

dependencies {
    implementation(projects.core.config)
    implementation(projects.settings.domain)
}