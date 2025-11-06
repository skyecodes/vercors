package app.vercors.launcher.cli

import app.vercors.launcher.common.data.CommonDataModule
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.module
import org.koin.ksp.generated.startKoin

fun main(args: Array<String>) {
    configureLogging()
    val koin = CLIApplication.startKoin {
        modules(module {
            single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        })
    }.koin
    runBlocking {
        MainCommand(koin)
            .subcommands(ListCommand(), CreateCommand(), LaunchCommand(), DeleteCommand())
            .main(args)
    }
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

@KoinApplication(modules = [CommonDataModule::class])
object CLIApplication

expect fun configureLogging()