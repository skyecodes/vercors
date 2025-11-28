package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.usecase.DeleteInstanceUseCase
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument

class DeleteCommand : VercorsCommandBase("delete") {
    private val id by argument(help = "Instance ID")

    override suspend fun execute() {
        val deleteInstance = koin.get<DeleteInstanceUseCase>()
        val deletedInstance = deleteInstance(id)
        echo("Instance ${deletedInstance.name} deleted")
    }

    override fun help(context: Context): String = "Delete an instance"
}
