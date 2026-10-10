package app.vercors.feature.java.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MojangJavaInfo(
    val files: Map<String, File>
) {
    @Serializable
    sealed interface File {
        @SerialName("directory")
        @Serializable
        data object Directory : File

        @Serializable
        @SerialName("file")
        data class Normal(
            val downloads: Downloads,
            val executable: Boolean,
        ) : File

        @Serializable
        @SerialName("link")
        data class Link(
            val target: String,
        ) : File

        @Serializable
        data class Downloads(
            val lzma: Download? = null,
            val raw: Download,
        )

        @Serializable
        data class Download(
            val sha1: String,
            val size: Long,
            val url: String
        )
    }
}
