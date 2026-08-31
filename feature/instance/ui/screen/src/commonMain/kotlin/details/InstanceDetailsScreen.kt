package app.vercors.feature.instance.ui.details

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.ui.Screen

data class InstanceDetailsScreen(
    val instanceId: InstanceId,
    val initialTab: InstanceDetailsTab? = null,
    val autoLaunch: Boolean = false,
) : Screen
