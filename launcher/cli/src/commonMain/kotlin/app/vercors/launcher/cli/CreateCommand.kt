package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.Instance
import app.vercors.launcher.common.domain.instance.InstanceRepository
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.koin.core.Koin

class CreateCommand : SuspendingCliktCommand("create") {
    private val koin by requireObject<Koin>()
    override fun help(context: Context): String = "Create a new instance"
    private val name by option(help = "Instance name").prompt("Instance name")
    private val gameVersion by option(help = "Minecraft version").default("latest-release")

    override suspend fun run() {
        val instanceRepository = koin.get<InstanceRepository>()
        val instance = Instance(name = name, gameVersion = gameVersion)
        instanceRepository.saveInstance(instance)
        echo("Instance created with ID: ${instance.id}")
    }
}