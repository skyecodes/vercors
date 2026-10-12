package app.vercors.feature.instance.ui.install

import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.notificationInstallFailedMessage
import app.vercors.feature.instance.ui.notificationInstallFinishedMessage
import dev.nucleusframework.notification.common.notification
import org.jetbrains.compose.resources.getString

actual suspend fun notifyModpackInstallResult(instanceName: String, ok: Boolean) {
    notification(
        title = instanceName,
        message = getString(
            if (ok) {
                InstanceRes.string.notificationInstallFinishedMessage
            } else {
                InstanceRes.string.notificationInstallFailedMessage
            }
        ),
    ).send()
}
