package app.vercors.launcher.gui.account

import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.shared.ui.core.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val AccountModule = module {
    container { new(::AccountListContainer) }
    appNavigation<AccountListRoute> { _, _ ->
        AccountListScreen(
            container = container()
        )
    }
}
