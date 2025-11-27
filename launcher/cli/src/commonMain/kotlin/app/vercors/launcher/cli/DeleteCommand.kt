package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.usecase.DeleteInstanceUseCase
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
        val deleteInstance = koin.get<DeleteInstanceUseCase>()
        val result = deleteInstance(id)
        if (result)
            echo("Instance deleted!")
        else
            echo("Instance with ID '$id' not found", err = true)
    }
}