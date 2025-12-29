package app.vercors.launcher.common.data.system

import app.vercors.launcher.common.domain.system.OsArch
import app.vercors.launcher.common.domain.system.OsType
import app.vercors.launcher.common.domain.system.SystemService
import org.koin.core.annotation.Single

@Single
class SystemServiceImpl : SystemService {
    override val osType: OsType by lazy { getOsName() }
    override val osVersion: String by lazy { getOsVersion() }
    override val osArch: OsArch by lazy { getOsArch() }
}

internal expect fun getOsName(): OsType

internal expect fun getOsVersion(): String

internal expect fun getOsArch(): OsArch
