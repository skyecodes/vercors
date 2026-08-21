package app.vercors.feature.instance.data.mods

import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ModJarMetadataParserTest {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Real fabric.mod.json/mods.toml files carry many fields beyond what [FabricModJson]/[ForgeModsToml] model
     * (entrypoints, mixins, dependencies, ...). Both kotlinx.serialization's Json and ktoml's Toml reject
     * unknown keys by default, which made every real-world mod jar silently fall back to filename-derived
     * metadata — this pins down that unknown fields must be tolerated.
     */
    @Test
    fun parsesFabricModJsonWithUnknownFields() {
        val path = Path("build/diag-fabric.jar")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.entryFromSource(
                Path("fabric.mod.json"),
                Buffer().apply {
                    writeString(
                        """
                        {
                          "schemaVersion": 1,
                          "id": "examplemod",
                          "name": "Example Mod",
                          "version": "1.2.3",
                          "description": "An example mod",
                          "environment": "*",
                          "entrypoints": {},
                          "depends": {}
                        }
                        """.trimIndent()
                    )
                }
            )
        }
        val result = parseModJar(path, json)
        assertEquals("Example Mod", result.title)
        assertEquals("1.2.3", result.version)
        assertEquals("An example mod", result.description)
    }

    @Test
    fun parsesForgeModsTomlWithUnknownFields() {
        val path = Path("build/diag-forge.jar")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.entryFromSource(
                Path("META-INF/mods.toml"),
                Buffer().apply {
                    writeString(
                        """
                        modLoader = "javafml"
                        loaderVersion = "[47,)"
                        license = "MIT"

                        [[mods]]
                        modId = "examplemod"
                        version = "1.2.3"
                        displayName = "Example Mod"
                        description = "An example mod"
                        """.trimIndent()
                    )
                }
            )
        }
        val result = parseModJar(path, json)
        assertEquals("Example Mod", result.title)
        assertEquals("1.2.3", result.version)
    }

    /**
     * Legacy Forge `mcmod.info` (Minecraft 1.12.2 and earlier) is a bare JSON array, not an object like the other
     * three formats — and real mcmod.info files carry extra fields (mcversion, authorList, url, ...) beyond what
     * [McModInfoEntry] models, so unknown-field tolerance needs pinning down here too.
     */
    @Test
    fun parsesLegacyForgeMcModInfoWithUnknownFields() {
        val path = Path("build/diag-legacy-forge.jar")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.entryFromSource(
                Path("mcmod.info"),
                Buffer().apply {
                    writeString(
                        """
                        [
                          {
                            "modid": "examplemod",
                            "name": "Example Mod",
                            "description": "An example mod",
                            "version": "1.2.3",
                            "mcversion": "1.12.2",
                            "authorList": ["Author1"]
                          }
                        ]
                        """.trimIndent()
                    )
                }
            )
        }
        val result = parseModJar(path, json)
        assertEquals("Example Mod", result.title)
        assertEquals("1.2.3", result.version)
        assertEquals("An example mod", result.description)
    }
}
