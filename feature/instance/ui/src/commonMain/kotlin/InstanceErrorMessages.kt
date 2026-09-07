package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceLoadingError
import app.vercors.feature.instance.domain.model.InstanceNotFound
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.localimport.LocalImportExtractionFailed
import app.vercors.feature.instance.domain.model.localimport.LocalImportNotPrismFormat
import app.vercors.feature.instance.domain.model.localimport.LocalImportParseFailed
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.ManualDownloadFailed
import app.vercors.feature.project.domain.model.ManualDownloadIntegrityMismatch
import org.jetbrains.compose.resources.stringResource

val InstanceValidationError.message: String
    @Composable get() = when (this) {
        InstanceValidationError.EmptyInstanceId -> stringResource(InstanceRes.string.errorEmptyInstanceId)
        InstanceValidationError.NameTooShort -> stringResource(InstanceRes.string.errorNameTooShort)
    }

val InstanceLaunchError.message: String
    @Composable get() = stringResource(
        InstanceRes.string.errorLaunch, when (this) {
            is InstanceLaunchError.AccountLoading -> stringResource(InstanceRes.string.errorAccountLoading, cause)
            is InstanceLaunchError.GameVersion -> stringResource(InstanceRes.string.errorGameVersion, cause)
            is InstanceLaunchError.ModLoaderInstall -> stringResource(InstanceRes.string.errorModLoaderInstall, cause)
            is InstanceLaunchError.InstanceNotFound -> stringResource(InstanceRes.string.errorNotFound, id)
            is InstanceLaunchError.NoCompatibleJava -> stringResource(
                InstanceRes.string.errorNoCompatibleJava,
                majorVersion
            )
            InstanceLaunchError.LoginRequired -> stringResource(InstanceRes.string.errorLoginRequired)
        }
    )

val InstanceNotFound.message: String @Composable get() = stringResource(InstanceRes.string.errorNotFound, id)

val InstanceLoadingError.message: String @Composable get() = stringResource(InstanceRes.string.errorLoading, id, cause)

val InstanceError.message: String
    @Composable get() = when (this) {
        is InstanceLoadingError -> this.message
        is InstanceNotFound -> this.message
    }

val ManualDownloadError.message: String
    @Composable get() = when (this) {
        ManualDownloadFailed -> stringResource(InstanceRes.string.errorManualDownloadFailed)
        ManualDownloadIntegrityMismatch -> stringResource(InstanceRes.string.errorManualDownloadIntegrityMismatch)
    }

val LocalImportError.message: String
    @Composable get() = when (this) {
        LocalImportNotPrismFormat -> stringResource(InstanceRes.string.errorImportUnsupportedFormat)
        LocalImportParseFailed -> stringResource(InstanceRes.string.errorImportParseFailed)
        LocalImportExtractionFailed -> stringResource(InstanceRes.string.errorImportExtractionFailed)
    }
