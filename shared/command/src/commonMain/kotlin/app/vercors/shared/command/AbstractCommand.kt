package app.vercors.shared.command

import arrow.core.raise.Raise
import arrow.core.raise.recover
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError

abstract class AbstractCommand(name: String) : SuspendingCliktCommand(name) {
    override suspend fun run() {
        try {
            recover<CommandError, Unit>(
                block = { execute() },
                recover = { throw CliktError(it.message) }
            )
        } catch (e: CliktError) {
            throw e
        } catch (e: Exception) {
            throw CliktError(e.message, e)
        }
    }

    context(_: Raise<CommandError>)
    abstract suspend fun execute()
}
