package app.vercors.feature.launcher.domain.service

import app.vercors.feature.game.domain.model.GameVersionDetails.Argument

/** Flattens Mojang argument templates and expands their `${variable}` placeholders. */
fun List<Argument>.resolve(variables: Map<String, String>): List<String> = flatMap { argument ->
    when (argument) {
        is Argument.Basic -> listOf(argument.value)
        is Argument.Conditional -> argument.value
    }
}.map { it.substituteVariables(variables) }

fun String.substituteVariables(variables: Map<String, String>): String =
    variables.entries.fold(this) { acc, (key, value) -> acc.replace($$"${$$key}", value) }
