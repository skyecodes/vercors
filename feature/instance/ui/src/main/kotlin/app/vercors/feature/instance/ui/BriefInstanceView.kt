package app.vercors.feature.instance.ui

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId

data class BriefInstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String
)

fun Instance.toBriefView(): BriefInstanceView = BriefInstanceView(
    id = id,
    name = name,
    version = gameVersion,
    loader = loaderName,
    type = typeName
)
