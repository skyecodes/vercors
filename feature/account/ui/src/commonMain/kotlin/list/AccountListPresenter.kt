package app.vercors.feature.account.ui.list

import androidx.compose.runtime.*
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.core.ui.state.update
import app.vercors.feature.account.domain.model.AccountError
import app.vercors.feature.account.domain.model.AccountLoadingError
import app.vercors.feature.account.domain.model.AccountNotFound
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.usecase.*
import app.vercors.shared.domain.usecase.CopyToClipboardUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

private val logger = KotlinLogging.logger {}

@CircuitInject(AccountListScreen::class, AppScope::class)
@Inject
class AccountListPresenter(
    private val observeAccountList: ObserveAccountListUseCase,
    private val login: LoginUseCase,
    private val logout: LogoutUseCase,
    private val selectAccount: SelectAccountUseCase,
    private val getAccountSkin: GetAccountSkinUseCase,
    private val copyToClipboard: CopyToClipboardUseCase
) : Presenter<AccountListState, AccountListEvent> {
    @Composable
    override fun present(): Store<AccountListState, AccountListEvent> {
        val presenterScope = rememberCoroutineScope()
        val accountList by observeAccountListView()
        var loginJob by remember { mutableStateOf<Job?>(null) }
        val loginState = remember { mutableStateOf(LoginStateView()) }
        val (promptLogin, setPromptLogin) = remember { mutableStateOf(false) }
        val loginDialog = remember { LoginDialog(loginState) { url -> copyToClipboard(url) } }

        return AccountListState(
            accountList = accountList,
            promptLogin = promptLogin,
            loginDialog = loginDialog,
        ) + { event ->
            when (event) {
                AccountListEvent.CancelLogin -> {
                    loginJob?.cancel(CancellationException("Login cancelled by user"))
                    setPromptLogin(false)
                }

                is AccountListEvent.CopyToClipboard -> copyToClipboard(event.data)
                AccountListEvent.Login -> {
                    loginState.value = LoginStateView()
                    setPromptLogin(true)
                    loginJob = presenterScope.launch { collectLoginState(loginState) }.apply {
                        invokeOnCompletion {
                            if (it != null && it !is CancellationException) onFailure(it, loginState)
                        }
                    }
                }

                is AccountListEvent.Logout -> presenterScope.launch { handleAccountError { logout(event.accountId) } }
                is AccountListEvent.SelectAccount -> presenterScope.launch { handleAccountError { selectAccount(event.accountId) } }
            }
        }
    }

    @Composable
    private fun observeAccountListView(): State<ProgressiveOutcome<AccountLoadingError, AccountListView>> =
        produceAndCollectAsProgressiveState {
            observeAccountList().map { outcome ->
                outcome.map { list ->
                    AccountListView(list.map { account ->
                        AccountView(
                            username = account.username,
                            id = account.id,
                            skin = getAccountSkin(account.id)
                        )
                    })
                }
            }
        }

    private suspend fun handleAccountError(block: suspend Raise<AccountError>.() -> Unit) {
        either { block() }.onLeft {
            when (it) {
                is AccountLoadingError -> logger.error { "Failed to load account list" }
                is AccountNotFound -> logger.error { "Account not found" }
            }
        }
    }

    private suspend fun collectLoginState(
        state: MutableState<LoginStateView>,
    ) = login().collect {
        when (it) {
            is LoginState.RequireLogin -> {
                state.update {
                    copy(
                        isLoggingIn = true,
                        url = it.url
                    )
                }
            }

            is LoginState.Progress -> state.update {
                copy(
                    isLoggingIn = true,
                    url = null,
                    progress = it.step.progress
                )
            }

            is LoginState.Success -> state.update {
                copy(
                    isLoggingIn = false,
                    url = null,
                    progress = null,
                    success = true,
                )
            }

            is LoginState.Error -> state.update {
                copy(
                    isLoggingIn = false,
                    url = null,
                    progress = null,
                    error = it.error,
                )
            }
        }
    }

    private fun onFailure(error: Throwable, loginState: MutableState<LoginStateView>) {
        logger.error(error) { "Login failed" }
        loginState.update {
            copy(
                isLoggingIn = false,
                url = null,
                progress = null,
                error = error
            )
        }
    }
}
