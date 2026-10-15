package app.vercors.feature.instance.ui.details.launch

import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.shared.ui.Event

sealed interface InstanceLaunchEvent : Event {
    data object OnLaunchClick : InstanceLaunchEvent
    data class OnQuickJoinClick(val target: QuickPlayTarget) : InstanceLaunchEvent
    data object OnCancelClick : InstanceLaunchEvent
    data object OnStopClick : InstanceLaunchEvent
    data object OnKillClick : InstanceLaunchEvent
    data object OnInstallJavaConfirm : InstanceLaunchEvent
    data object OnInstallJavaDismiss : InstanceLaunchEvent
    data object OnJavaInstallProgressDismiss : InstanceLaunchEvent
    data object OnLoginDismiss : InstanceLaunchEvent
}
