package app.vercors.launcher.common.domain

fun plural(collection: Collection<*>): String = if (collection.size > 1) "s" else ""