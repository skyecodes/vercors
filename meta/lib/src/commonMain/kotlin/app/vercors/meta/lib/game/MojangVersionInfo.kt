package app.vercors.meta.lib.game

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class MojangVersionInfo(
    val arguments: Arguments? = null,
    val assetIndex: AssetIndex,
    val assets: String,
    val complianceLevel: Int? = null,
    val downloads: Downloads,
    val id: String,
    val javaVersion: JavaVersion? = null,
    val libraries: List<Library>,
    val logging: Logging? = null,
    val mainClass: String,
    val minecraftArguments: String? = null,
    val minimumLauncherVersion: Int,
    val releaseTime: Instant,
    val time: Instant,
    val type: MojangReleaseType
) {
    @Serializable
    data class Arguments(
        val game: List<Argument>,
        val jvm: List<Argument>
    )

    @Serializable(ArgumentSerializer::class)
    sealed interface Argument {
        @JvmInline
        value class Basic(
            val value: String
        ) : Argument

        @Serializable
        data class Conditional(
            val rules: List<Rule>,
            val value: List<String>
        ) : Argument
    }

    @Serializable
    data class AssetIndex(
        val id: String,
        val sha1: String,
        val size: Long,
        val totalSize: Long,
        val url: String
    )

    @Serializable
    data class Downloads(
        val client: Download,
        @SerialName("client_mappings")
        val clientMappings: Download? = null,
        val server: Download? = null,
        @SerialName("server_mappings")
        val serverMappings: Download? = null,
        @SerialName("windows_server")
        val windowsServer: Download? = null
    ) {
        @Serializable
        data class Download(
            val sha1: String,
            val size: Long,
            val url: String
        )
    }

    @Serializable
    data class JavaVersion(
        val component: String,
        val majorVersion: Int
    )

    @Serializable
    data class Library(
        val downloads: Downloads,
        val extract: Extract? = null,
        val name: String,
        val natives: Map<String, String>? = null,
        val rules: List<Rule>? = null
    ) {
        @Serializable
        data class Downloads(
            val artifact: Artifact? = null,
            val classifiers: Map<String, Artifact>? = null
        ) {
            @Serializable
            data class Artifact(
                val path: String,
                val sha1: String,
                val size: Long,
                val url: String
            )
        }

        @Serializable
        data class Extract(
            val exclude: List<String>
        )
    }

    @Serializable
    data class Logging(
        val client: Client
    ) {
        @Serializable
        data class Client(
            val argument: String,
            val file: File,
            val type: Type
        )

        enum class Type {
            @SerialName("log4j2-xml")
            Log4j2Xml
        }

        @Serializable
        data class File(
            val id: String,
            val sha1: String,
            val size: Long,
            val url: String
        )
    }

    @Serializable
    data class Rule(
        val action: Action,
        val os: Os? = null,
        val features: Map<String, Boolean>? = null
    ) {
        @Serializable
        data class Os(
            val name: String? = null,
            val version: String? = null,
            val arch: String? = null
        )

        enum class Action {
            @SerialName("allow")
            Allow,

            @SerialName("disallow")
            Disallow
        }
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    internal class ArgumentSerializer : KSerializer<Argument> {
        override val descriptor = buildSerialDescriptor(
            serialName = Argument::class.qualifiedName!!,
            kind = PolymorphicKind.SEALED
        ) {
            element(
                elementName = Argument.Basic::class.qualifiedName!!,
                descriptor = PrimitiveSerialDescriptor(
                    serialName = Argument.Basic::class.qualifiedName!! + ".value",
                    kind = PrimitiveKind.STRING
                ),
                isOptional = true
            )
            element(
                elementName = Argument.Conditional::class.qualifiedName!!,
                descriptor = Argument.Conditional.serializer().descriptor,
                isOptional = true
            )
        }

        override fun deserialize(decoder: Decoder): Argument {
            val jsonDecoder = decoder as JsonDecoder
            val element = jsonDecoder.decodeJsonElement()
            return if (element is JsonPrimitive)
                Argument.Basic(element.content)
            else {
                val obj = element.jsonObject
                val rules = jsonDecoder.json.decodeFromJsonElement<List<Rule>>(obj["rules"]!!)
                val value = when (val v = obj["value"]) {
                    is JsonPrimitive -> listOf(v.jsonPrimitive.content)
                    is JsonArray -> v.jsonArray.map { it.jsonPrimitive.content }
                    else -> error("Unexpected value type: $v")
                }
                Argument.Conditional(rules, value)

            }
        }

        override fun serialize(encoder: Encoder, value: Argument) {
            val jsonEncoder = encoder as JsonEncoder
            when (value) {
                is Argument.Basic -> jsonEncoder.encodeString(value.value)
                is Argument.Conditional -> jsonEncoder.json.encodeToJsonElement<Argument.Conditional>(value)
            }
        }
    }
}
