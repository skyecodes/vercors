package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.vercors.core.ui.theme.error
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.success
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.InstanceStatus
import com.composeunstyled.theme.ThemeToken
import org.jetbrains.compose.resources.stringResource

val InstanceStatus.text: String
    @Composable get() = when (this) {
        InstanceStatus.Idle -> stringResource(InstanceRes.string.idle)
        is InstanceStatus.Launching.ValidatingToken -> stringResource(InstanceRes.string.validatingToken)
        is InstanceStatus.Launching.RefreshingToken -> stringResource(InstanceRes.string.refreshingToken)
        is InstanceStatus.Launching.LoadingMetadata -> stringResource(InstanceRes.string.loadingMetadata)
        is InstanceStatus.Launching.InstallingLoader ->
            stringResource(InstanceRes.string.installingLoader, loaderType.displayName)

        is InstanceStatus.Launching.ValidatingFiles -> stringResource(InstanceRes.string.validatingFiles)
        is InstanceStatus.Launching.DownloadingFiles -> stringResource(InstanceRes.string.downloadingGameFiles)
        is InstanceStatus.Installing.ResolvingFiles -> stringResource(InstanceRes.string.resolvingFiles)
        is InstanceStatus.Installing.DownloadingFiles -> stringResource(InstanceRes.string.downloadingModpackFiles)
        is InstanceStatus.Installing.AwaitingManualDownloads -> stringResource(InstanceRes.string.awaitingManualDownloads)
        is InstanceStatus.Installing.Interrupted -> stringResource(InstanceRes.string.installationInterrupted)
        is InstanceStatus.Installing.ExtractingFiles -> stringResource(InstanceRes.string.extractingFiles)
        is InstanceStatus.Running -> stringResource(InstanceRes.string.running)
        is InstanceStatus.Stopping -> stringResource(InstanceRes.string.stopping)
        is InstanceStatus.Failed -> stringResource(InstanceRes.string.failed)
    }

val InstanceStatus.progress: Float?
    get() = when (this) {
        is InstanceStatus.Launching.ValidatingFiles -> progress
        is InstanceStatus.Launching.DownloadingFiles -> progress
        is InstanceStatus.Installing.ResolvingFiles -> progress
        is InstanceStatus.Installing.DownloadingFiles -> progress
        is InstanceStatus.Installing.ExtractingFiles -> progress
        else -> null
    }

val InstanceStatus.badgeTone: ThemeToken<Color>?
    get() = when (this) {
        InstanceStatus.Idle -> null
        is InstanceStatus.Launching, is InstanceStatus.Installing -> primary
        is InstanceStatus.Running -> success
        is InstanceStatus.Stopping, is InstanceStatus.Failed -> error
    }
