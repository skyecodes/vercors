package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceLoadingError
import app.vercors.feature.instance.domain.model.InstanceNotFound
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
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
            is InstanceLaunchError.InstanceNotFound -> stringResource(InstanceRes.string.errorNotFound, id)
        }
    )

val InstanceNotFound.message: String @Composable get() = stringResource(InstanceRes.string.errorNotFound, id)

val InstanceLoadingError.message: String @Composable get() = stringResource(InstanceRes.string.errorLoading, id, cause)

val InstanceError.message: String
    @Composable get() = when (this) {
        is InstanceLoadingError -> this.message
        is InstanceNotFound -> this.message
    }
