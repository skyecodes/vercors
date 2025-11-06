package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.shared.domain.awaitValueOrThrow
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import org.koin.core.Koin

class DeleteCommand : SuspendingCliktCommand("delete") {
    private val koin by requireObject<Koin>()
    override fun help(context: Context): String = "Delete an instance"
    private val id by argument(help = "Instance ID")

    override suspend fun run() {
        val instanceRepository = koin.get<InstanceRepository>()
        val instances = instanceRepository.instances.awaitValueOrThrow()
        instances.find { it.id == id }?.let {
            instanceRepository.deleteInstance(it)
            echo("Instance deleted!")
        } ?: echo("Instance with ID '$id' not found", err = true)
    }
}