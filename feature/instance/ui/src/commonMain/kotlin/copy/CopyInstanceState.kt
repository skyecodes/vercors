package app.vercors.feature.instance.ui.copy

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId

@Immutable
data class CopyInstanceState(
    val instanceName: String,
    val isCopying: Boolean = false,
) {
    val canCopyInstance: Boolean = instanceName.length >= 3 && !isCopying
}

data class CopyInstanceTarget(val id: InstanceId, val name: String)
