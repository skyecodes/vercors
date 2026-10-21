package app.vercors.feature.instance.domain.model.options

import app.vercors.feature.instance.domain.model.InstanceId
import kotlin.jvm.JvmInline

sealed interface InstanceOptionsError {
    @JvmInline
    value class InstanceNotFound(val id: InstanceId) : InstanceOptionsError

    @JvmInline
    value class InstanceRunning(val id: InstanceId) : InstanceOptionsError

    data object NoDefaultOptions : InstanceOptionsError
    data object ReadFailed : InstanceOptionsError
    data object WriteFailed : InstanceOptionsError
}
