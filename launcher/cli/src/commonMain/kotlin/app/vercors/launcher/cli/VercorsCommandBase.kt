package app.vercors.launcher.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.requireObject
import org.koin.core.Koin

abstract class VercorsCommandBase(name: String) : SuspendingCliktCommand(name) {
    protected val koin by requireObject<Koin>()

    override suspend fun run() {
        try {
            execute()
        } catch (e: CliktError) {
            throw e
        } catch (e: Exception) {
            throw CliktError(e.message, e)
        }
    }

    abstract suspend fun execute()
}