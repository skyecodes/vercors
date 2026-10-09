package app.vercors.feature.instance.ui.details.servers

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstalledServer
import app.vercors.feature.instance.ui.details.stripMinecraftFormattingForDisplay

@Immutable
data class InstanceServersState(val servers: List<InstalledServerView>?)

@Immutable
data class InstalledServerView(
    val name: String,
    val address: String,
)

fun InstalledServer.toView(): InstalledServerView = InstalledServerView(
    name = name.stripMinecraftFormattingForDisplay(),
    address = address,
)
