package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.InstanceRepository
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
        val instanceRepository = koin.get<InstanceRepository>()
        val instances = instanceRepository.instances.awaitValueOrThrow()
        if (instances.isEmpty()) {
            echo("No instances found")
        } else {
            echo("Found ${instances.size} instance(s)")
            echo(table {
                header { row("ID", "Name") }
                body {
                    instances.forEach { row(it.id, it.name) }
                }
            })
        }
    }
}