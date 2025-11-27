package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.usecase.LaunchInstanceUseCase
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import org.koin.core.Koin

class LaunchCommand : SuspendingCliktCommand("launch") {
    private val koin by requireObject<Koin>()
    override fun help(context: Context): String = "Launch an instance"
    private val id by argument(help = "Instance ID").validate {
        require(it.isNotEmpty()) { "Instance ID cannot be empty" }
    }

    override suspend fun run() {
        val launchInstance = koin.get<LaunchInstanceUseCase>()
        launchInstance(id)
    }
}