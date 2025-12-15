package app.vercors.launcher.gui.config

import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.shared.ui.component.AppText
import org.koin.dsl.module

val ConfigurationModule = module {
    appNavigation<ConfigurationRoute> { _, _ ->
        AppText("Settings")
    }
}
