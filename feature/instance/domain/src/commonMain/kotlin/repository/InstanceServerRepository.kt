package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.InstalledServer
import app.vercors.feature.instance.domain.model.InstanceId
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceServerRepository {
    fun observeServers(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledServer>>
}
