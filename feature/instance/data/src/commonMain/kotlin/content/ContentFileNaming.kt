package app.vercors.feature.instance.data.content

private const val DISABLED_SUFFIX = ".disabled"

/** Whether [fileName] (the actual on-disk name) is a content file of the given [extension], enabled or disabled. */
internal fun isContentFile(fileName: String, extension: String): Boolean =
    fileName.endsWith(".$extension") || fileName.endsWith(".$extension$DISABLED_SUFFIX")

internal fun isContentFileEnabled(actualFileName: String): Boolean = !actualFileName.endsWith(DISABLED_SUFFIX)

/** The file's stable identity, used as the cache store key — never includes the `.disabled` suffix. */
internal fun contentLogicalFileName(actualFileName: String): String = actualFileName.removeSuffix(DISABLED_SUFFIX)

/**
 * [actualFileName] stripped of both `.disabled` and the extension, for deriving a fallback title. Also strips
 * raw Minecraft formatting codes — some packs are distributed with their in-game display name (which can contain
 * `§`-prefixed color/style codes) reused verbatim as the file name.
 */
internal fun contentBaseName(actualFileName: String, extension: String): String =
    contentLogicalFileName(actualFileName).removeSuffix(".$extension").stripMinecraftFormatting()
