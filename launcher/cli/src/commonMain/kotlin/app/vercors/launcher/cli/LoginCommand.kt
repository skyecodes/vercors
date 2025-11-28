package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.account.model.LoginState
import app.vercors.launcher.common.domain.account.model.LoginStep
import app.vercors.launcher.common.domain.account.usecase.LoginUseCase
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarContextLayout
import com.github.ajalt.mordant.widgets.progress.text
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LoginCommand : VercorsCommandBase("login") {
    override suspend fun execute() = coroutineScope {
        val login = koin.get<LoginUseCase>()
        val progress = progressBarContextLayout {
            text { context }
            progressBar()
            completed()
        }.animateInCoroutine(
            terminal = terminal,
            context = "Waiting for login",
            total = LoginStep.entries.size.toLong()
        )

        launch { progress.execute() }

        login().collect {
            when (it) {
                is LoginState.RequireLogin -> {
                    echo("Please login to your Microsoft account by clicking on the following link:")
                    echo(it.url)
                    echo(" ")
                }

                is LoginState.Progress -> {
                    progress.update {
                        completed = it.step.number.toLong()
                        context = it.step.description
                    }
                }

                is LoginState.Success -> echo("Username: ${it.account.username}\n ")
                is LoginState.Error -> throw CliktError("Failed to login: ${it.error.message}", it.error)
            }
        }
    }

    override fun help(context: Context): String = "Login to your Microsoft account"
}