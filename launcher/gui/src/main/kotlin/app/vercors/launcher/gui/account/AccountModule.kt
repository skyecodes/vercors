package app.vercors.launcher.gui.account

import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.label
import app.vercors.shared.ui.textStyles
import com.composeunstyled.theme.Theme
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val AccountModule = module {
    navigation<AccountListRoute> {
        AppText("Account", style = Theme[textStyles][label])
    }
}
