package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.value

/** Appends `_1`, `_2`, ... to [id] until it no longer collides with an existing instance's ID. */
internal suspend fun InstanceRepository.uniqueInstanceId(id: InstanceId): InstanceId {
    val existingIds = instances.now().value.keys
    if (id !in existingIds) return id
    var suffix = 1
    while ("${id}_$suffix" in existingIds) suffix++
    return "${id}_$suffix"
}
