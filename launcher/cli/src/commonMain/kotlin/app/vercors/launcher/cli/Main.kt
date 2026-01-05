package app.vercors.launcher.cli

import app.vercors.feature.account.domain.usecase.LoginUseCase
import app.vercors.feature.account.domain.usecase.LogoutUseCase
import app.vercors.feature.account.domain.usecase.ObserveAccountListUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.*

fun main(args: Array<String>) {
    configureLogging()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    runBlocking {
        with(appGraph) {
            MainCommand().subcommands(
                ListCommand(observeInstanceList),
                CreateCommand(observeGameVersionList, createInstance),
                LaunchCommand(launchInstance),
                DeleteCommand(deleteInstance),
                AccountsCommand(observeAccountList),
                LoginCommand(login),
                LogoutCommand(logout)
            ).main(args)
        }
    }
    scope.cancel()
}

class MainCommand : SuspendingCliktCommand("vercors-cli") {
    override val invokeWithoutSubcommand = true
    override val printHelpOnEmptyArgs = true

    override fun aliases(): Map<String, List<String>> = mapOf(
        "ls" to listOf("list"),
        "c" to listOf("create"),
        "new" to listOf("create"),
        "l" to listOf("launch"),
        "run" to listOf("launch"),
        "d" to listOf("delete"),
        "rm" to listOf("delete"),
    )

    override suspend fun run() = Unit
}

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val observeInstanceList: ObserveInstanceListUseCase
    val observeGameVersionList: ObserveGameVersionListUseCase
    val createInstance: CreateInstanceUseCase
    val launchInstance: LaunchInstanceUseCase
    val deleteInstance: DeleteInstanceUseCase
    val observeAccountList: ObserveAccountListUseCase
    val login: LoginUseCase
    val logout: LogoutUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides coroutineScope: CoroutineScope): AppGraph
    }
}

internal expect fun configureLogging()
