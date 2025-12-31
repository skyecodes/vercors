package app.vercors.feature.launcher.domain.service

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.model.InstanceLaunchState
import kotlinx.coroutines.flow.Flow

interface LauncherService {
    fun launch(instance: Instance, account: Account, features: List<InstanceLaunchFeature>): Flow<InstanceLaunchState>
}
