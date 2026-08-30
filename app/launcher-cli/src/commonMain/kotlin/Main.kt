package app.vercors.launcher.cli

import app.vercors.core.domain.AppConstants
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.data.AppPaths
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import dev.zacsweers.metro.*
import kotlinx.coroutines.*
import kotlinx.io.files.Path

fun main(args: Array<String>) {
    configureLoggingPre()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val appGraph = createGraphFactory<AppGraph.Factory>().create(scope)
    configureLoggingPost(appGraph.appPaths.logs)
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
        "conf" to listOf("config"),
    )

    override suspend fun run() = Unit
}

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val appPaths: AppPaths
    val commands: Set<AbstractCommand>

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_ID_KEY)
    fun provideAppId(): String = BuildConfig.APP_ID

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_NAME_KEY)
    fun provideAppName(): String = BuildConfig.APP_NAME

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_VERSION_KEY)
    fun provideAppVersion(): String = BuildConfig.APP_VERSION

    @Provides
    @SingleIn(AppScope::class)
    @Named(AppConstants.APP_VENDOR_KEY)
    fun provideAppVendor(): String = BuildConfig.APP_VENDOR

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides coroutineScope: CoroutineScope): AppGraph
    }
}

expect fun configureLoggingPre()

expect fun configureLoggingPost(logDir: Path)
