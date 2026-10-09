package app.vercors.feature.java.data.repository

import app.vercors.core.system.SystemService

internal expect suspend fun platformScanJavaInstallationsWindows(systemService: SystemService): List<CandidateJavaPath>
internal expect suspend fun platformScanJavaInstallationsLinux(systemService: SystemService): List<CandidateJavaPath>
internal expect suspend fun platformScanJavaInstallationsMacOs(systemService: SystemService): List<CandidateJavaPath>
