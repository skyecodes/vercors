package app.vercors.feature.instance.data.content

private const val DISABLED_SUFFIX = ".disabled"

internal fun isContentFile(fileName: String, extension: String): Boolean =
    fileName.endsWith(".$extension") || fileName.endsWith(".$extension$DISABLED_SUFFIX")

internal fun isContentFileEnabled(actualFileName: String): Boolean = !actualFileName.endsWith(DISABLED_SUFFIX)

internal fun contentLogicalFileName(actualFileName: String): String = actualFileName.removeSuffix(DISABLED_SUFFIX)

// Some packs reuse their §-formatted in-game name as the file name
internal fun contentBaseName(actualFileName: String, extension: String): String =
    contentLogicalFileName(actualFileName).removeSuffix(".$extension").stripMinecraftFormatting()
