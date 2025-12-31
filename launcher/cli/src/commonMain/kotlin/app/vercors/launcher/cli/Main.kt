package app.vercors.launcher.cli

import app.vercors.feature.account.data.AccountDataModule
import app.vercors.feature.account.domain.AccountDomainModule
import app.vercors.feature.config.data.ConfigDataModule
import app.vercors.feature.game.data.GameDataModule
import app.vercors.feature.game.domain.GameDomainModule
import app.vercors.feature.instance.data.InstanceDataModule
import app.vercors.feature.instance.domain.InstanceDomainModule
import app.vercors.feature.launcher.data.LauncherDataModule
import app.vercors.feature.launcher.domain.LauncherDomainModule
import app.vercors.feature.project.data.ProjectDataModule
import app.vercors.feature.project.domain.ProjectDomainModule
import app.vercors.meta.client.MetaClientModule
import app.vercors.shared.data.SharedDataModule
import app.vercors.shared.data.commonDataProperties
import app.vercors.shared.domain.SharedDomainModule
import app.vercors.shared.domain.service.SystemService
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

@KoinApplication(
    modules = [
        MetaClientModule::class,
        SharedDataModule::class, SharedDomainModule::class,
        AccountDataModule::class, AccountDomainModule::class,
        ConfigDataModule::class,
        GameDataModule::class, GameDomainModule::class,
        InstanceDataModule::class, InstanceDomainModule::class,
        LauncherDataModule::class, LauncherDomainModule::class,
        ProjectDataModule::class, ProjectDomainModule::class,
    ]
)
object CLIApplication

internal expect fun configureLogging()

private class SystemCommand : VercorsCommandBase("system") {
    override suspend fun execute() {
        koin.get<SystemService>().let {
            echo("${it.osType} ${it.osVersion} (${it.osArch})")
        }
    }
}
