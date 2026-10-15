package app.vercors.feature.project.data.repository

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.update.InstanceUpdates
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceUpdateRepositoryImpl : InstanceUpdateRepository {
    private val updates = MutableStateFlow<Map<InstanceId, InstanceUpdates>>(emptyMap())
    private val checking = MutableStateFlow<Set<InstanceId>>(emptySet())

    override fun observeAll(): Flow<Map<InstanceId, InstanceUpdates>> = updates.asStateFlow()

    override fun get(instanceId: InstanceId): InstanceUpdates? = updates.value[instanceId]

    override fun set(instanceId: InstanceId, updates: InstanceUpdates) {
        this.updates.update { it + (instanceId to updates) }
    }

    override fun removeContent(instanceId: InstanceId, fileNames: Set<String>) {
        updates.update { all ->
            val current = all[instanceId] ?: return@update all
            all + (instanceId to current.copy(content = current.content.filterNot { it.fileName in fileNames }))
        }
    }

    override fun clear(instanceId: InstanceId) {
        updates.update { it - instanceId }
    }

    override fun observeChecking(): Flow<Set<InstanceId>> = checking.asStateFlow()

    override fun setChecking(instanceId: InstanceId, checking: Boolean) {
        this.checking.update { if (checking) it + instanceId else it - instanceId }
    }
}
