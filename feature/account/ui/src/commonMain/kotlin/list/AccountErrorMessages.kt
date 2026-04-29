package app.vercors.feature.account.ui.list

import androidx.compose.runtime.Composable
import app.vercors.feature.account.domain.model.AccountLoadingError
import app.vercors.feature.account.ui.AccountRes
import app.vercors.feature.account.ui.errorAccountLoadingFailed
import org.jetbrains.compose.resources.stringResource

val AccountLoadingError.message: String
    @Composable get() = stringResource(AccountRes.string.errorAccountLoadingFailed)
