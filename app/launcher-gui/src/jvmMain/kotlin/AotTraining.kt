package app.vercors.launcher.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import app.vercors.feature.account.ui.list.AccountListScreen
import app.vercors.feature.config.ui.ConfigurationScreen
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import arrow.core.raise.context.either
import com.slack.circuit.runtime.Navigator
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.application.aotTraining
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid


@Composable
internal fun NucleusApplicationScope.runAotTraining(navigator: Navigator, appGraph: AppGraph) {
    aotTraining(12.seconds)
    val instanceId = remember { Uuid.random().toHexString() }

    runAfter(0.seconds) {
        either {
            appGraph.createInstance(
                Instance(
                    name = "aot-training",
                    id = instanceId,
                    gameVersion = "26.2"
                )
            )
        }
    }

    runAfter(2.seconds) {
        navigator.goTo(InstanceListScreen)
    }

    runAfter(3.seconds) {
        navigator.goTo(InstanceDetailsScreen(instanceId))
    }

    runAfter(4.seconds) {
        navigator.goTo(ProjectListScreen())
    }

    runAfter(6.seconds) {
        navigator.goTo(ProjectDetailsScreen(ProjectId("P7dR8mSH", "modrinth")))
    }

    runAfter(8.seconds) {
        navigator.goTo(ConfigurationScreen)
    }

    runAfter(9.seconds) {
        navigator.goTo(AccountListScreen)
    }

    runAfter(10.seconds) {
        either {
            appGraph.deleteInstance(instanceId)
        }
    }
}

@Composable
private fun runAfter(duration: kotlin.time.Duration, block: suspend () -> Unit) {
    LaunchedEffect(Unit) {
        delay(duration)
        block()
    }
}
