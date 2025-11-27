package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.usecase.CreateInstanceUseCase
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.koin.core.Koin

class CreateCommand : SuspendingCliktCommand("create") {
    private val koin by requireObject<Koin>()
    override fun help(context: Context): String = "Create a new instance"
    private val name by option(help = "Instance name").prompt(text = "Instance name")
    private val gameVersion by option(help = "Minecraft version").prompt(
        text = "Minecraft version",
        default = "latest-release"
    )

    override suspend fun run() {
        val createInstance = koin.get<CreateInstanceUseCase>()
        val instance = Instance(name = name, gameVersion = gameVersion)
        createInstance(instance)
        echo("Instance created with ID: ${instance.id}")
    }
}