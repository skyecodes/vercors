package app.vercors.feature.instance.domain.model

import kotlin.jvm.JvmInline

sealed interface InstanceError {
    val id: InstanceId
}

data class InstanceLoadingError(override val id: InstanceId, val cause: Any) : InstanceError

@JvmInline
value class InstanceNotFound(override val id: InstanceId) : InstanceError

sealed interface InstanceValidationError {
    data object EmptyInstanceId : InstanceValidationError
    data object NameTooShort : InstanceValidationError
}
