plugins {
    id("app.vercors.launcher.presentation")
}

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.account.domain)
}