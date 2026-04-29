package app.vercors.feature.account.command

import app.vercors.feature.account.domain.service.LoginState
import app.vercors.feature.account.domain.service.LoginStep
import app.vercors.feature.account.domain.usecase.LoginUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarContextLayout
import com.github.ajalt.mordant.widgets.progress.text
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@ContributesIntoSet(AppScope::class)
class LoginCommand(private val login: LoginUseCase) : AbstractCommand("login") {
    context(_: Raise<CommandError>)
    override suspend fun execute() = coroutineScope {
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
