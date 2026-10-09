package app.vercors.core.domain

import opensavvy.progress.Progress

fun Progress.toFractionOrNull(): Float? = when (this) {
    is Progress.Loading.Quantified -> normalized.toFloat()
    Progress.Done -> 1f
    Progress.Loading.Unquantified -> null
}
