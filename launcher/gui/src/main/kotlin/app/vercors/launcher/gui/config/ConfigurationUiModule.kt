package app.vercors.launcher.gui.config

import app.vercors.core.ui.component.AppText
import app.vercors.shared.ui.appNavigation
import org.koin.dsl.module

val ConfigurationUiModule = module {
    appNavigation<ConfigurationRoute> { _, _ ->
        AppText("Settings")
    }
}
