package app.vercors.launcher.demo.data

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.BriefInstanceView
import kotlin.time.Clock

const val MONIFACTORY_ID: InstanceId = "monifactory"
const val CURSEFORGE_PLATFORM_ID = "curseforge"

fun recentInstances(): List<BriefInstanceView> = listOf(
    briefModpackInstance(MONIFACTORY_ID, "Monifactory", "Forge", "1.20.1"),
    briefModpackInstance("all-the-mods-10-atm10", "All the Mods 10 - ATM10", "NeoForge", "1.21.1"),
    briefModpackInstance("ftb-infinity-evolved", "FTB Infinity Evolved", "Forge", "1.7.10"),
)

private fun briefModpackInstance(id: InstanceId, name: String, loader: String, version: String): BriefInstanceView =
    BriefInstanceView(
        id = id,
        name = name,
        version = version,
        loader = loader,
        type = "Modpack",
        iconFilePath = "/icons/$id.webp",
        status = InstanceStatus.Idle,
        updatedAt = Clock.System.now(),
        modpackInfo = InstanceModpackInfo(platformId = CURSEFORGE_PLATFORM_ID, projectId = id, fileId = id),
    )
