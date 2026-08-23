package app.vercors.feature.account.ui.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.appAnimateFloatAsState
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppProgressIndicator
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.account.ui.AccountRes
import app.vercors.feature.account.ui.accountAddedSuccessfully
import app.vercors.feature.account.ui.logIn
import app.vercors.feature.account.ui.requireMicrosoftLogin
import app.vercors.shared.ui.*
import app.vercors.shared.ui.overlay.DialogOverlay
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource

class LoginDialog(
    private val loginState: State<LoginStateView>,
    private val onCopyUrl: (String) -> Unit,
) : DialogOverlay<Unit>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        val state by loginState
        val uriHandler = LocalUriHandler.current

        AppDialog(onClickOutside = {}) {
            AppForm {
                AppText(
                    text = stringResource(AccountRes.string.logIn),
                    style = Theme[textStyles][title]
                )

                state.url?.let { url ->
                    AppText(
                        text = stringResource(AccountRes.string.requireMicrosoftLogin),
                        modifier = Modifier.width(500.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppIconTextButton(
                            onClick = { uriHandler.openUri(url) },
                            text = stringResource(SharedRes.string.openInBrowser),
                            imageVector = Lucide.ExternalLink
                        )
                        AppIconTextButton(
                            onClick = { onCopyUrl(url) },
                            text = stringResource(SharedRes.string.copyLink),
                            imageVector = Lucide.Link
                        )
                    }
                }

                state.progress?.let {
                    val progress by appAnimateFloatAsState(it)

                    AppProgressIndicator(
                        progress = progress,
                        modifier = Modifier.width(500.dp),
                    )
                }

                state.error?.let {
                    if (it !is CancellationException) AppError(it)
                }

                if (state.success) {
                    AppText(text = stringResource(AccountRes.string.accountAddedSuccessfully))
                    AppIconTextButton(
                        onClick = { navigator.finish(Unit) },
                        text = stringResource(SharedRes.string.done),
                        imageVector = Lucide.Check,
                    )
                } else {
                    AppIconTextButton(
                        onClick = { navigator.finish(Unit) },
                        text = stringResource(SharedRes.string.cancel),
                        imageVector = Lucide.X,
                        backgroundColor = Theme[colors][error],
                        contentColor = Theme[colors][onError],
                    )
                }
            }
        }
    }
}
