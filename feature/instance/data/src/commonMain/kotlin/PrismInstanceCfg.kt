package app.vercors.feature.instance.data

private val CFG_CHARS_NEEDING_QUOTES = setOf(',', ';', '"', '=', '\\', '#')

internal fun parseInstanceCfg(text: String): Map<String, String> = text.lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() && !it.startsWith("#") }
    .mapNotNull { line ->
        val idx = line.indexOf('=')
        if (idx < 0) null else line.substring(0, idx).trim() to line.substring(idx + 1).trim().unquoteCfgValue()
    }
    .toMap()

// Prism reads instance.cfg through QSettings, which splits an unquoted value containing ',' into a string list
internal fun formatInstanceCfg(values: Map<String, String>): String = buildString {
    appendLine("[General]")
    for ((key, value) in values) appendLine("$key=${value.quoteCfgValueIfNeeded()}")
}

private fun String.quoteCfgValueIfNeeded(): String =
    if (any { it in CFG_CHARS_NEEDING_QUOTES } || trim() != this) {
        "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    } else {
        this
    }

private fun String.unquoteCfgValue(): String =
    if (length >= 2 && startsWith('"') && endsWith('"')) {
        substring(1, length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
    } else {
        this
    }
