package app.vercors.feature.account.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedContent
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.layout.AppSection
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.*
import app.vercors.feature.account.ui.*
import app.vercors.shared.ui.*
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import dev.zacsweers.metro.AppScope
import opensavvy.state.progressive.ProgressiveOutcome
import org.jetbrains.compose.resources.stringResource

@CircuitInject(AccountListScreen::class, AppScope::class)
@Composable
fun AccountList(
    store: Store<AccountListState, AccountListEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    with(eventSink) {
        if (state.promptLogin) {
            OverlayEffect {
                show(state.loginDialog)
                eventSink(AccountListEvent.CancelLogin)
            }
        }

        when (val outcome = state.accountList) {
            is ProgressiveOutcome.Success -> AppAnimatedContent(outcome.value, contentKey = { it.accounts.isEmpty() }) {
                if (it.accounts.isEmpty()) NoAccountsFoundDisplay(
                    modifier = modifier
                )
                else AccountList(
                    accountList = it,
                    modifier = modifier
                )
            }

            is ProgressiveOutcome.Failure -> AppError(outcome.failure.message)
            is ProgressiveOutcome.Incomplete -> AppLoading()
        }
    }
}

@Composable
context(_: EventSink<AccountListEvent>)
private fun NoAccountsFoundDisplay(
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = stringResource(AccountRes.string.noAccountFound)
        )

        LoginButton()
    }
}

@Composable
context(_: EventSink<AccountListEvent>)
private fun AccountList(
    accountList: AccountListView,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val scrollState = rememberScrollState()

        LaunchedEffect(accountList.selectedAccount) {
            scrollState.animateScrollTo(0)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).padding(vertical = 8.dp).verticalScroll(scrollState)
        ) {
            accountList.selectedAccount?.let { currentAccount ->
                AppSection(
                    icon = Lucide.UserCheck,
                    text = stringResource(AccountRes.string.selectedAccount)
                ) {
                    AccountCard(
                        account = currentAccount,
                        isSelected = true
                    )
                }
            }

            if (accountList.otherAccounts.isNotEmpty()) {
                AppSection(
                    icon = Lucide.Users,
                    text = stringResource(AccountRes.string.otherAccounts)
                ) {
                    accountList.otherAccounts.forEach {
                        AccountCard(
                            account = it,
                            isSelected = false
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LoginButton(
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
            )
        }
    }
}

@Composable
context(eventSink: EventSink<AccountListEvent>)
private fun AccountCard(
    account: AccountView,
    isSelected: Boolean,
) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp).appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(account.skin.bodyUrl),
            contentDescription = account.username,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).heightIn(max = 80.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            AppText(
                text = account.username,
                style = Theme[textStyles][subtitle],
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = "UUID: ${account.id}"
                )
                AppTooltip(
                    panel = {
                        AppTooltipPanel(
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            AppText(stringResource(SharedRes.string.copy))
                        }
                    },
                    anchor = {
                        AppIcon(
                            imageVector = Lucide.ClipboardCopy,
                            contentDescription = stringResource(SharedRes.string.copy),
                            modifier = Modifier.size(16.dp)
                                .clickableText { eventSink(AccountListEvent.CopyToClipboard(account.id.toString())) }
                        )
                    }
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            AppAnimatedVisibility(!isSelected) {
                AppIconTextButton(
                    text = stringResource(SharedRes.string.select),
                    imageVector = Lucide.UserCheck,
                    onClick = { eventSink(AccountListEvent.SelectAccount(account.id)) },
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AppIconTextButton(
                text = stringResource(AccountRes.string.logOut),
                imageVector = Lucide.LogOut,
                onClick = { eventSink(AccountListEvent.Logout(account.id)) },
                backgroundColor = Theme[colors][error],
                contentColor = Theme[colors][onError],
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
context(eventSink: EventSink<AccountListEvent>)
private fun LoginButton(
    modifier: Modifier = Modifier,
) {
    AppIconTextButton(
        onClick = { eventSink(AccountListEvent.Login) },
        text = stringResource(AccountRes.string.logIn),
        imageVector = Lucide.LogIn,
        backgroundColor = Theme[colors][primary],
        contentColor = Theme[colors][onPrimary],
        modifier = modifier
    )
}
