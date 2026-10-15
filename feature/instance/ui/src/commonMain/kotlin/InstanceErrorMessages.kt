package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceLoadingError
import app.vercors.feature.instance.domain.model.InstanceNotFound
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.options.InstanceOptionsError
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.pack.LocalPackError
import app.vercors.feature.project.domain.model.update.InstanceUpdateError
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
        ManualDownloadError.DownloadFailed -> stringResource(InstanceRes.string.errorManualDownloadFailed)
        ManualDownloadError.IntegrityMismatch -> stringResource(InstanceRes.string.errorManualDownloadIntegrityMismatch)
    }

val InstanceUpdateError.message: String
    @Composable get() = when (this) {
        is InstanceUpdateError.ModpackLookupFailed, is InstanceUpdateError.ManifestFailed ->
            stringResource(InstanceRes.string.errorUpdateLookupFailed)

        InstanceUpdateError.InstanceNotFound -> stringResource(InstanceRes.string.errorUpdateInstanceNotFound)
        InstanceUpdateError.SaveFailed -> stringResource(InstanceRes.string.errorUpdateSaveFailed)
        InstanceUpdateError.DownloadFailed -> stringResource(InstanceRes.string.errorUpdateDownloadFailed)
    }

val LocalImportError.message: String
    @Composable get() = when (this) {
        LocalImportError.NotPrismFormat -> stringResource(InstanceRes.string.errorImportUnsupportedFormat)
        LocalImportError.ParseFailed -> stringResource(InstanceRes.string.errorImportParseFailed)
        LocalImportError.ExtractionFailed -> stringResource(InstanceRes.string.errorImportExtractionFailed)
    }

val LocalPackError.message: String
    @Composable get() = when (this) {
        LocalPackError.UnsupportedFormat -> stringResource(InstanceRes.string.errorImportUnsupportedFormat)
        LocalPackError.ParseFailed -> stringResource(InstanceRes.string.errorImportParseFailed)
        LocalPackError.UnsupportedPackVersion -> stringResource(InstanceRes.string.errorImportUnsupportedPackVersion)
        LocalPackError.InstallFailed -> stringResource(InstanceRes.string.errorLocalPackInstallFailed)
        LocalPackError.InstanceNotFound -> stringResource(InstanceRes.string.errorUpdateInstanceNotFound)
    }

val InstanceOptionsError.message: String
    @Composable get() = when (this) {
        is InstanceOptionsError.InstanceNotFound -> stringResource(InstanceRes.string.errorNotFound, id)
        is InstanceOptionsError.InstanceRunning -> stringResource(InstanceRes.string.errorOptionsInstanceRunning)
        InstanceOptionsError.NoDefaultOptions,
        InstanceOptionsError.ReadFailed,
        InstanceOptionsError.WriteFailed -> stringResource(InstanceRes.string.errorOptionsFailed)
    }
