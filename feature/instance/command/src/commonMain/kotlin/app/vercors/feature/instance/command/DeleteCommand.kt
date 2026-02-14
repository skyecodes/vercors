package app.vercors.feature.instance.command

import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class DeleteCommand(private val deleteInstance: DeleteInstanceUseCase) : AbstractCommand("delete") {
    private val id by argument(help = "Instance ID")

    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val deletedInstance = withError({ CommandError("Instance with ID $id not found") }) { deleteInstance(id) }
        echo("Instance ${deletedInstance.name} deleted")
    }

    override fun help(context: Context): String = "Delete an instance"
}
