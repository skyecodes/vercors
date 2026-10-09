package app.vercors.feature.instance.data.content

import kotlinx.serialization.Serializable

@Serializable
internal data class ContentFileMetadata(
    val title: String,
    val description: String?,
    val version: String?,
    val loader: String?,
    val iconExtension: String?,
    val providers: List<ContentProviderMetadata> = emptyList()
)

@Serializable
internal data class ContentProviderMetadata(
    val platformId: String,
    val projectId: String,
    val fileId: String
)
