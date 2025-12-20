package app.vercors.launcher.gui.instance

import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId

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
