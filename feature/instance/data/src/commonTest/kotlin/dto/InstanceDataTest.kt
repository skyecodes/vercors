package app.vercors.feature.instance.data.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class InstanceDataTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun roundTripsPendingManualDownloads() {
        val data = InstanceData(
            name = "Test Instance",
            gameVersion = "1.21.1",
            modLoader = null,
            modpackInfo = InstanceModpackInfoData("curseforge", "123", "456"),
            pendingManualDownloads = listOf(
                PendingManualDownloadData(
                    fileName = "restricted-mod.jar",
                    downloadUrl = "https://example.com/restricted-mod.jar",
                    size = 1024L,
                    sha1 = "abc123",
                    md5 = "def456",
                    contentKind = ContentKindData.Mod,
                    platformId = "curseforge",
                    providerProjectId = "789",
                    providerFileId = "101112",
                )
            ),
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
            lastPlayedAt = null,
            playTime = 5.minutes,
        )

        val encoded = json.encodeToString(InstanceData.serializer(), data)
        val decoded = json.decodeFromString(InstanceData.serializer(), encoded)

        assertEquals(data, decoded)
    }

    /**
     * Existing on-disk instance JSON files (from before [InstanceData.pendingManualDownloads] existed) don't have
     * this key at all — this is the backward-compatibility guarantee the launcher-restart fix relies on.
     */
    @Test
    fun missingPendingManualDownloadsKeyDecodesToEmptyList() {
        val legacyJson = """
            {
                "name": "Legacy Instance",
                "gameVersion": "1.20.1",
                "modLoader": null,
                "modpackInfo": null,
                "createdAt": "1970-01-01T00:00:00Z",
                "updatedAt": "1970-01-01T00:00:00Z",
                "lastPlayedAt": null,
                "playTime": "PT0S"
            }
        """.trimIndent()

        val decoded = json.decodeFromString(InstanceData.serializer(), legacyJson)

        assertEquals(emptyList(), decoded.pendingManualDownloads)
    }

    @Test
    fun roundTripsInstallStage() {
        val data = InstanceData(
            name = "Test Instance",
            gameVersion = "1.21.1",
            modLoader = null,
            modpackInfo = InstanceModpackInfoData("curseforge", "123", "456"),
            installStage = InstanceInstallStageData.DownloadingFiles,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
            lastPlayedAt = null,
            playTime = 5.minutes,
        )

        val encoded = json.encodeToString(InstanceData.serializer(), data)
        val decoded = json.decodeFromString(InstanceData.serializer(), encoded)

        assertEquals(data, decoded)
    }

    /**
     * Existing on-disk instance JSON files (from before [InstanceData.installStage] existed) don't have this key at
     * all — this is the backward-compatibility guarantee the interrupted-install-resume fix relies on.
     */
    @Test
    fun missingInstallStageKeyDecodesToNull() {
        val legacyJson = """
            {
                "name": "Legacy Instance",
                "gameVersion": "1.20.1",
                "modLoader": null,
                "modpackInfo": null,
                "createdAt": "1970-01-01T00:00:00Z",
                "updatedAt": "1970-01-01T00:00:00Z",
                "lastPlayedAt": null,
                "playTime": "PT0S"
            }
        """.trimIndent()

        val decoded = json.decodeFromString(InstanceData.serializer(), legacyJson)

        assertEquals(null, decoded.installStage)
    }
}
