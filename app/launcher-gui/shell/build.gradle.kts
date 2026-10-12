plugins {
    id("app.vercors.compose")
    id("app.vercors.web")
}

dependencies {
    commonMainImplementation(projects.core.ui)
    commonMainImplementation(projects.shared.ui)
    commonMainImplementation(projects.feature.account.ui.screen)
    commonMainImplementation(projects.feature.config.ui.screen)
    commonMainImplementation(projects.feature.home.ui.screen)
    commonMainImplementation(projects.feature.instance.ui.screen)
    commonMainImplementation(projects.feature.project.ui.screen)
    commonMainImplementation(libs.composables.icons.lucide)
}

compose.resources {
    packageOfResClass = "app.vercors.launcher.shell"
    nameOfResClass = "ShellRes"
    publicResClass = true
}
