package app.vercors.launcher.cli

import app.vercors.launcher.common.data.CommonDataModule
import app.vercors.launcher.common.data.commonDataProperties
import app.vercors.launcher.common.domain.CommonDomainModule
import app.vercors.launcher.common.domain.system.SystemService
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import kotlinx.coroutines.*
import org.koin.core.Koin
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.module
import org.koin.ksp.generated.startKoin

fun main(args: Array<String>) {
    configureLogging()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val koin = CLIApplication.startKoin {
        commonDataProperties()
        modules(module {
            single { scope }
        })
    }.koin
    runBlocking {
        MainCommand(koin)
            .subcommands(
                ListCommand(),
                CreateCommand(),
                LaunchCommand(),
                DeleteCommand(),
                AccountsCommand(),
                LoginCommand(),
                LogoutCommand(),
                SystemCommand()
            )
            .main(args)
    }
    scope.cancel()
}

class MainCommand(private val koin: Koin) : SuspendingCliktCommand("vercors-cli") {
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

    override suspend fun run() {
        currentContext.findOrSetObject { koin }
    }
}

@KoinApplication(modules = [CommonDomainModule::class, CommonDataModule::class])
object CLIApplication

internal expect fun configureLogging()

private class SystemCommand : VercorsCommandBase("system") {
    override suspend fun execute() {
        koin.get<SystemService>().let {
            echo("${it.osType} ${it.osVersion} (${it.osArch})")
        }
    }
}
