package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.usecase.ObserveInstanceListUseCase
import app.vercors.launcher.common.domain.plural
import app.vercors.shared.domain.awaitValueOrThrow
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table

class ListCommand : VercorsCommandBase("list") {
    override suspend fun execute() {
        val observeInstanceList = koin.get<ObserveInstanceListUseCase>()
        val instances = observeInstanceList().awaitValueOrThrow()
        if (instances.isEmpty()) {
            echo("No instance found. Create a new instance using 'vercors create'.")
        } else {
            echo("Found ${instances.size} instance${plural(instances)}:")
            echo(table {
                borderType = BorderType.SQUARE_DOUBLE_SECTION_SEPARATOR
                header { row("ID", "Name", "Version", "Loader", "Type") }
                body {
                    instances.forEach { row(it.id, it.name, it.gameVersion, it.loaderName, it.typeName) }
                }
            })
        }
    }

    override fun help(context: Context): String = "Lists existing instances"
}