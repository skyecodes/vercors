package app.vercors.feature.launcher.data.service

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext

internal actual val platformPathSeparator: String = ":"
internal actual suspend fun ProducerScope<InstanceLaunchEvent>.runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext
) {
    // TODO implement
}
