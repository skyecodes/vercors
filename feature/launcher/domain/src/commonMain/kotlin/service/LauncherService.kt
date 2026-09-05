package app.vercors.feature.launcher.domain.service

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import kotlinx.coroutines.flow.Flow

interface LauncherService {
    fun launch(
        instance: Instance,
        account: Account,
        version: GameVersionDetails,
        assetIndex: GameAssetIndex,
        features: List<InstanceLaunchFeature>,
        javaExecutable: String,
        jvmMemoryOptions: JvmMemoryOptions,
        nativeLibraryOptions: NativeLibraryOptions,
        quickPlayTarget: QuickPlayTarget? = null
    ): Flow<InstanceLaunchEvent>
}
