package app.vercors.feature.account.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.domain.Result
import app.vercors.core.domain.resultOrNull
import app.vercors.core.ui.animation.AppAnimatedContent
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.animation.appAnimateContentSize
import app.vercors.core.ui.animation.appAnimateItem
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.modifier.scrollGradient
import app.vercors.core.ui.theme.*
import app.vercors.feature.account.ui.*
import app.vercors.shared.ui.*
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource

@CircuitInject(AccountListScreen::class, AppScope::class)
@Composable
fun AccountList(
    store: Store<AccountListState, AccountListEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    with(eventSink) {
        state.accountList.resultOrNull()?.let { res ->
            when (res) {
                is Result.Failure -> AppError(res.error)
                is Result.Success -> {
                    AppAnimatedContent(res.data, contentKey = { it.accounts.isEmpty() }) {
                        if (it.accounts.isEmpty()) NoAccountsFoundDisplay(
                            loginState = state.loginState,
                            modifier = modifier
                        )
                        else AccountList(
                            accountList = it,
                            loginState = state.loginState,
                            modifier = modifier
                        )
                    }
                }
            }
        } ?: AppLoading()
    }
}

@Composable
context(_: EventSink<AccountListEvent>)
private fun NoAccountsFoundDisplay(
    loginState: LoginStateView,
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

        LoginDisplay(
            loginState = loginState
        )
    }
}

@Composable
context(_: EventSink<AccountListEvent>)
private fun AccountList(
    accountList: AccountListView,
    loginState: LoginStateView,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val listState = rememberLazyListState()

        LaunchedEffect(accountList.selected) {
            listState.animateScrollToItem(0)
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).scrollGradient(
                listState = listState,
                orientation = Orientation.Vertical,
                color = Theme[colors][background],
            )
        ) {
            accountList.selectedAccount?.let { currentAccount ->
                item("selected") {
                    AppText(
                        text = stringResource(AccountRes.string.selectedAccount),
                        style = Theme[textStyles][title],
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }
                item(currentAccount.id) {
                    AccountCard(
                        account = currentAccount,
                        isSelected = true
                    )
                }
            }

            if (accountList.otherAccounts.isNotEmpty()) {
                item("other") {
                    AppText(
                        text = stringResource(AccountRes.string.otherAccounts),
                        style = Theme[textStyles][title],
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }
                items(accountList.otherAccounts, key = { it.id }) { account ->
                    AccountCard(
                        account = account,
                        isSelected = false
                    )
                }
            }
        }
        LoginDisplay(
            loginState = loginState,
            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
        )
    }
}

@Composable
context(eventSink: EventSink<AccountListEvent>)
private fun LazyItemScope.AccountCard(
    account: AccountView,
    isSelected: Boolean,
) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp).appCard().appAnimateItem().padding(8.dp).fillMaxWidth(),
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
fun LoginDisplay(
    loginState: LoginStateView,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().appAnimateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!loginState.isLoggingIn) {
            AppIconTextButton(
                onClick = { eventSink(AccountListEvent.Login) },
                text = stringResource(AccountRes.string.logIn),
                imageVector = Lucide.LogIn,
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary]
            )
        } else {
            AppIconTextButton(
                onClick = { eventSink(AccountListEvent.CancelLogin) },
                text = stringResource(SharedRes.string.cancel),
                imageVector = Lucide.X,
                backgroundColor = Theme[colors][error],
                contentColor = Theme[colors][onError]
            )
        }

        loginState.url?.let {
            AppText(
                text = stringResource(AccountRes.string.requireMicrosoftLogin),
                modifier = Modifier.widthIn(max = 500.dp)
            )
            AppIconTextButton(
                onClick = { eventSink(AccountListEvent.CopyToClipboard(it)) },
                text = stringResource(SharedRes.string.copyLink),
                imageVector = Lucide.Link,
                backgroundColor = Theme[colors][surface2]
            )
        }

        loginState.progress?.let {
            AppProgressIndicator(
                progress = it,
                modifier = Modifier.width(500.dp),
            )
        }

        loginState.error?.let {
            if (it !is CancellationException) AppError(it)
        }
    }
}
