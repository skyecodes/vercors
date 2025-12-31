plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.instance.domain)
    implementation(projects.feature.launcher.domain)
    implementation(projects.feature.game.domain)
    implementation(projects.feature.game.ui)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.instance.ui"
    nameOfResClass = "InstanceRes"
    publicResClass = true
}
