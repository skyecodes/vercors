package app.vercors.feature.launcher.domain.model

import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.instance.domain.model.InstanceId
import kotlin.jvm.JvmInline

sealed interface InstanceLaunchError {
    @JvmInline
    value class InstanceNotFound(val id: InstanceId) : InstanceLaunchError

    @JvmInline
    value class AccountLoading(val cause: Any) : InstanceLaunchError

    @JvmInline
    value class GameVersion(val cause: GameVersionError) : InstanceLaunchError

    @JvmInline
    value class ModLoaderInstall(val cause: ModLoaderInstallError) : InstanceLaunchError
}
