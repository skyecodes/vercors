package app.vercors.core.domain

fun <T> Collection<T>.toggle(value: T): List<T> = if (value in this) this - value else this + value
