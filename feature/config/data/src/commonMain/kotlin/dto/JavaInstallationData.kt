package app.vercors.feature.config.data.dto

import app.vercors.shared.domain.model.JavaInstallation
import kotlinx.serialization.Serializable

@Serializable
data class JavaInstallationData(
    val id: String,
    val path: String,
    val majorVersion: Int,
    val versionString: String,
    val vendor: String,
    val arch: String,
    val isManuallyAdded: Boolean = false,
)

internal fun JavaInstallationData.toDomain(): JavaInstallation = JavaInstallation(
    id = id,
    path = path,
    majorVersion = majorVersion,
    versionString = versionString,
    vendor = vendor,
    arch = arch,
    isManuallyAdded = isManuallyAdded,
)

internal fun JavaInstallation.toData(): JavaInstallationData = JavaInstallationData(
    id = id,
    path = path,
    majorVersion = majorVersion,
    versionString = versionString,
    vendor = vendor,
    arch = arch,
    isManuallyAdded = isManuallyAdded,
)
