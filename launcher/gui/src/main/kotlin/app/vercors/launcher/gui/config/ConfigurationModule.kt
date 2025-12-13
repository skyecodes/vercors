package app.vercors.launcher.gui.config

import app.vercors.shared.ui.component.AppText
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val ConfigurationModule = module {
    navigation<ConfigurationRoute> {
        AppText("Settings")
    }
}
