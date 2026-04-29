package app.vercors.launcher.cli

import app.vercors.shared.command.AbstractCommand
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.*
import kotlinx.io.files.Path

fun main(args: Array<String>) {
    configureLoggingPre()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    val logDir = Path(appGraph.appPath, "logs")
    configureLoggingPost(logDir)
    runBlocking {
        MainCommand().subcommands(appGraph.commands).main(args)
    }
    scope.cancel()
}

class MainCommand : SuspendingCliktCommand("vercors") {
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
    val appPath: Path
    val commands: Set<AbstractCommand>

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides coroutineScope: CoroutineScope): AppGraph
    }
}

expect fun configureLoggingPre()

expect fun configureLoggingPost(logDir: Path)
