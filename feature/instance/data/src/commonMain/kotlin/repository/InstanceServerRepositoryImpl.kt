package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.servers.InstanceServerScannerService
import app.vercors.feature.instance.domain.model.InstalledServer
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceServerRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceServerRepositoryImpl(
    private val scannerService: InstanceServerScannerService
) : InstanceServerRepository {
    override fun observeServers(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledServer>> =
        scannerService.observe(instanceId)
}
