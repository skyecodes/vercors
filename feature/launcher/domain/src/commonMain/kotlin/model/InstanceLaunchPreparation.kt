package app.vercors.feature.launcher.domain.model

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions

data class InstanceLaunchPreparation(
    val instance: Instance,
    val account: Account,
    val version: GameVersionDetails,
    val assetIndex: GameAssetIndex,
    val javaExecutable: String,
    val jvmMemoryOptions: JvmMemoryOptions,
    val nativeLibraryOptions: NativeLibraryOptions,
)
