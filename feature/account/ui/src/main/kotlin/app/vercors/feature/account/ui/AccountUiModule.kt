package app.vercors.feature.account.ui

import app.vercors.feature.account.ui.list.AccountListContainer
import app.vercors.feature.account.ui.list.AccountListRoute
import app.vercors.feature.account.ui.list.AccountListScreen
import app.vercors.shared.ui.appNavigation
import app.vercors.shared.ui.container
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val AccountUiModule = module {
    container { new(::AccountListContainer) }
    appNavigation<AccountListRoute> { _, _ ->
        AccountListScreen(
            container = container()
        )
    }
}
