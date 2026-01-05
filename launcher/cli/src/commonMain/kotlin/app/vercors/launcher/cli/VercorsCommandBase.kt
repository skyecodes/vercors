package app.vercors.launcher.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError

abstract class VercorsCommandBase(name: String) : SuspendingCliktCommand(name) {
    override suspend fun run() {
        try {
            execute()
        } catch (e: CliktError) {
            e.printStackTrace()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw CliktError(e.message, e)
        }
    }

    abstract suspend fun execute()
}
