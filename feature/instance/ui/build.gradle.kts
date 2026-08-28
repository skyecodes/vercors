plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    commonMainApi(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.launcher.domain)
    commonMainImplementation(projects.feature.game.domain)
    commonMainImplementation(projects.feature.game.ui)
    commonMainImplementation(projects.feature.project.domain)
    commonMainImplementation(projects.feature.project.ui.screen)
    commonMainImplementation(projects.feature.account.ui)
    commonMainImplementation(projects.feature.config.domain)
    commonMainImplementation(libs.kotlinx.datetime)
    commonMainImplementation(libs.filekit.dialogs.compose)
    commonMainImplementation(libs.nucleus.notification.common)
    commonMainImplementation(libs.nucleus.notification.windows)
    commonMainImplementation(libs.nucleus.notification.linux)
    commonMainImplementation(libs.nucleus.notification.macos)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.instance.ui"
    nameOfResClass = "InstanceRes"
    publicResClass = true
}
