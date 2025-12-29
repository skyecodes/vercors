package app.vercors.launcher.common.domain.instance

import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceLaunchFeature
import app.vercors.launcher.common.domain.instance.model.InstanceLaunchState
import kotlinx.coroutines.flow.Flow

interface LauncherService {
    fun launch(instance: Instance, account: Account, features: List<InstanceLaunchFeature>): Flow<InstanceLaunchState>
}
