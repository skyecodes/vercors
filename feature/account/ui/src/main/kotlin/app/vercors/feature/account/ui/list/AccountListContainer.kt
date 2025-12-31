package app.vercors.feature.account.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Resource
import app.vercors.core.domain.map
import app.vercors.feature.account.domain.model.AccountId
import app.vercors.feature.account.domain.model.AccountSkin
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.service.LoginStep
import app.vercors.feature.account.domain.usecase.*
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.domain.usecase.OpenBrowserUseCase
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class AccountListContainer(
    private val observeAccountList: ObserveAccountListUseCase,
    private val login: LoginUseCase,
    private val logout: LogoutUseCase,
    private val selectAccount: SelectAccountUseCase,
    private val getAccountSkin: GetAccountSkinUseCase,
    private val openBrowser: OpenBrowserUseCase,
    private val copyToClipboard: CopyToClipboardUseCase
) : Container<AccountListState, AccountListIntent, AccountListAction> {
    private var loginJob: Job? = null

    override val store = store(initial = AccountListState()) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                AccountListIntent.LogIn -> {
                    loginJob = launch {
                        login().collect {
                            when (it) {
                                is LoginState.RequireLogin -> {
                                    openBrowser(it.url)
                                    updateState {
                                        AccountListState(
                                            loginState = loginState.copy(
                                                isLoggingIn = true,
                                                url = it.url,
                                            )
                                        )
                                    }
                                }

                                is LoginState.Progress -> updateState {
                                    AccountListState(
                                        loginState = loginState.copy(
                                            isLoggingIn = true,
                                            url = null,
                                            progress = it.step.number.toFloat() / LoginStep.entries.size
                                        )
                                    )
                                }

                                is LoginState.Success -> updateState {
                                    AccountListState(
                                        loginState = loginState.copy(
                                            isLoggingIn = false,
                                            url = null,
                                            progress = null,
                                        )
                                    )
                                }

                                is LoginState.Error -> updateState {
                                    AccountListState(
                                        loginState = loginState.copy(
                                            isLoggingIn = false,
                                            url = null,
                                            progress = null,
                                            error = it.error,
                                        )
                                    )
                                }
                            }
                        }
                    }
                    loginJob!!.invokeOnCompletion {
                        it?.let {
                            logger.error(it) { "Login failed" }
                            launch {
                                updateState {
                                    AccountListState(
                                        loginState = loginState.copy(
                                            isLoggingIn = false,
                                            url = null,
                                            progress = null,
                                            error = it,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                AccountListIntent.CancelLogIn -> loginJob?.cancel(CancellationException("Login cancelled by user"))
                is AccountListIntent.CopyToClipboard -> copyToClipboard(intent.data)

                is AccountListIntent.Logout -> launch { logout(intent.account.id.toString()) }
                is AccountListIntent.SelectAccount -> launch { selectAccount(intent.account.id) }
            }
        }

        whileSubscribed {
            observeAccountList().collect { resource ->
                updateState {
                    AccountListState(accountList = resource.map { list ->
                        AccountListView(
                            selected = list.selected,
                            accounts = list.accounts.map { account ->
                                AccountView(
                                    username = account.username,
                                    id = account.id,
                                    skin = getAccountSkin(account.id)
                                )
                            }
                        )
                    })
                }
            }
        }
    }
}

@Immutable
data class AccountListState(
    val accountList: Resource<AccountListView> = Resource.Unloaded,
    val loginState: LoginStateView = LoginStateView(),
) : MVIState

@Immutable
data class AccountListView(
    val selected: AccountId?,
    val accounts: List<AccountView>,
) {
    val selectedAccount: AccountView? = accounts.firstOrNull { it.id == selected }
    val otherAccounts: List<AccountView> = accounts.filter { it.id != selected }
}

@Immutable
data class AccountView(
    val id: AccountId,
    val username: String,
    val skin: AccountSkin
)

@Immutable
data class LoginStateView(
    val isLoggingIn: Boolean = false,
    val url: String? = null,
    val progress: Float? = null,
    val error: Throwable? = null,
)

sealed interface AccountListIntent : MVIIntent {
    data object LogIn : AccountListIntent
    data object CancelLogIn : AccountListIntent
    data class CopyToClipboard(val data: String) : AccountListIntent
    data class Logout(val account: AccountView) : AccountListIntent
    data class SelectAccount(val account: AccountView) : AccountListIntent
}

sealed interface AccountListAction : MVIAction
