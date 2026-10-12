package app.vercors.website

import kotlin.js.Date

fun formatDate(isoDate: String): String =
    Date(isoDate).toLocaleDateString("en-US", dateLocaleOptions {
        year = "numeric"
        month = "long"
        day = "numeric"
        timeZone = "UTC"
    })
