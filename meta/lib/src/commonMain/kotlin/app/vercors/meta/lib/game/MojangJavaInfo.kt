package app.vercors.meta.lib.game

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
            val lzma: Download,
            val raw: Download,
            val executable: Boolean,
        ) : File

        @Serializable
        data class Download(
            val sha1: String,
            val size: Long,
            val url: String
        )
    }
}
