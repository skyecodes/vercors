package app.vercors.feature.instance.domain.model.options

import app.vercors.feature.instance.domain.model.InstanceId

sealed interface OptionsSource {
    data class Instance(val instanceId: InstanceId) : OptionsSource
    data object Default : OptionsSource
}
