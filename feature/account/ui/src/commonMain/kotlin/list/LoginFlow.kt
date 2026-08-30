package app.vercors.feature.account.ui.list

import androidx.compose.runtime.MutableState
import app.vercors.core.ui.state.update
import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.usecase.LoginUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Starts (or restarts) the Microsoft/Xbox/Mojang login flow: resets [loginState], opens the login dialog
 * via [setPromptLogin], launches a collector of [login]'s [LoginState] flow on [scope], and records the
 * resulting [Job] via [setLoginJob] so the caller can later cancel it (e.g. a "Cancel" button in the login
 * dialog calling `loginJob?.cancel(CancellationException(...))`).
 *
 * Shared by [AccountListPresenter] (launches on its own `presenterScope`) and `InstanceDetailsPresenter`
 * (launches on its injected app-wide `globalScope`, so login can outlive navigating away from the Instance
 * Details screen mid-launch) — see CLAUDE.md's `DialogOverlay`/`invokeOnCompletion` gotchas for why the
 * `it !is CancellationException` guard below must stay exactly as-is.
 */
fun startLoginFlow(
    scope: CoroutineScope,
    login: LoginUseCase,
    loginState: MutableState<LoginStateView>,
    setPromptLogin: (Boolean) -> Unit,
    setLoginJob: (Job?) -> Unit,
) {
    loginState.value = LoginStateView()
    setPromptLogin(true)
    val job = scope.launch { collectLoginState(login, loginState) }
    job.invokeOnCompletion { if (it != null && it !is CancellationException) onLoginFlowFailure(it, loginState) }
    setLoginJob(job)
}

private suspend fun collectLoginState(login: LoginUseCase, state: MutableState<LoginStateView>) =
    login().collect {
        when (it) {
            is LoginState.RequireLogin -> state.update { copy(isLoggingIn = true, url = it.url) }
            is LoginState.Progress -> state.update { copy(isLoggingIn = true, url = null, progress = it.step.progress) }
            is LoginState.Success -> state.update {
                copy(isLoggingIn = false, url = null, progress = null, success = true)
            }

            is LoginState.Error -> state.update {
                copy(isLoggingIn = false, url = null, progress = null, error = it.error)
            }
        }
    }

private fun onLoginFlowFailure(error: Throwable, state: MutableState<LoginStateView>) {
    logger.error(error) { "Login failed" }
    state.update { copy(isLoggingIn = false, url = null, progress = null, error = error) }
}
