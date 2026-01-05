plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.launcher.domain)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.ui)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.instance.ui"
    nameOfResClass = "InstanceRes"
    publicResClass = true
}
