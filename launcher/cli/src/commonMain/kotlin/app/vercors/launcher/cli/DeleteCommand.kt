package app.vercors.launcher.cli

import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument

class DeleteCommand(private val deleteInstance: DeleteInstanceUseCase) : VercorsCommandBase("delete") {
    private val id by argument(help = "Instance ID")

    override suspend fun execute() {
        val deletedInstance = deleteInstance(id)
        echo("Instance ${deletedInstance.name} deleted")
    }

    override fun help(context: Context): String = "Delete an instance"
}
