package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.usecase.GetInstanceListUseCase
import app.vercors.launcher.common.domain.plural
import app.vercors.shared.domain.awaitValueOrThrow
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.mordant.table.table
import org.koin.core.Koin

class ListCommand : SuspendingCliktCommand("list") {
    private val koin by requireObject<Koin>()
    override fun help(context: Context): String = "Lists existing instances"

    override suspend fun run() {
        val getInstanceList = koin.get<GetInstanceListUseCase>()
        val instances = getInstanceList().awaitValueOrThrow()
        if (instances.isEmpty()) {
            echo("No instance found. Create a new instance using 'vercors create'.")
        } else {
            echo("Found ${instances.size} instance${plural(instances)}:")
            echo(table {
                header { row("ID", "Name", "Minecraft Version", "Loader", "Type") }
                body {
                    instances.forEach { row(it.id, it.name, it.gameVersion, it.loaderName, it.typeName) }
                }
            })
        }
    }
}