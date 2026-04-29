package app.vercors.feature.instance.command

import app.vercors.core.domain.filterIsRight
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.shared.command.AbstractCommand
import app.vercors.shared.command.CommandError
import app.vercors.shared.command.plural
import arrow.core.raise.Raise
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.value

@ContributesIntoSet(AppScope::class)
class ListCommand(private val observeInstanceList: ObserveInstanceListUseCase) : AbstractCommand("list") {
    context(_: Raise<CommandError>)
    override suspend fun execute() {
        val instances = observeInstanceList().now().value.filterIsRight()
        if (instances.isEmpty()) {
            echo("No instance found. Create a new instance using 'vercors create'.")
        } else {
            echo("Found ${instances.size} instance${plural(instances)}:")
            echo(table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header { row("ID", "Name", "Version", "Loader", "Type") }
                body {
                    instances.forEach {
                        row(
                            it.instance.id,
                            it.instance.name,
                            it.instance.gameVersion,
                            it.instance.modLoader?.type?.name ?: "Vanilla",
                            it.instance.modpackInfo?.let { "Modpack" } ?: "Custom")
                    }
                }
            })
        }
    }

    override fun help(context: Context): String = "Lists existing instances"
}
