package app.vercors.launcher.cli

fun plural(collection: Collection<*>): String = if (collection.size > 1) "s" else ""
