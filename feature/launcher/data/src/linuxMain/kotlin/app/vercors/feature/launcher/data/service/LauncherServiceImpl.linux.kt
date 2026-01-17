package app.vercors.feature.launcher.data.service

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext

internal actual val platformPathSeparator: String = ":"
internal actual fun runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext
): Flow<InstanceLogEvent> {
    // TODO implement
}
