package app.vercors.shared.command

fun plural(collection: Collection<*>): String = if (collection.size > 1) "s" else ""
