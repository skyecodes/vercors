package app.vercors.feature.instance.ui.details

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.ui.Route
import kotlinx.serialization.Serializable

@Serializable
data class InstanceDetailsRoute(val instanceId: InstanceId) : Route
