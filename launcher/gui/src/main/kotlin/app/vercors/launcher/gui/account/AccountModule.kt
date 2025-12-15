package app.vercors.launcher.gui.account

import app.vercors.launcher.gui.navigation.appNavigation
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.label
import app.vercors.shared.ui.textStyles
import com.composeunstyled.theme.Theme
import org.koin.dsl.module

val AccountModule = module {
    appNavigation<AccountListRoute> { _, _ ->
        AppText("Account", style = Theme[textStyles][label])
    }
}
