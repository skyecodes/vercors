package app.vercors.launcher.cli

import app.vercors.launcher.common.domain.instance.usecase.LaunchInstanceUseCase
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate

class LaunchCommand : VercorsCommandBase("launch") {
    private val id by argument(help = "Instance ID").validate {
        require(it.isNotEmpty()) { "Instance ID cannot be empty" }
    }

    override suspend fun execute() {
        val launchInstance = koin.get<LaunchInstanceUseCase>()
        launchInstance(id).collect {
        }
    }

    override fun help(context: Context): String = "Launch an instance"
}
