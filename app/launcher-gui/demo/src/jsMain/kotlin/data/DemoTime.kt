package app.vercors.launcher.demo.data

import kotlin.time.Clock
import kotlin.time.Instant

// Data was captured at this instant; shifting by its age keeps "x days ago" labels stable as the demo ages
private val DATA_CAPTURED_AT: Instant = Instant.parse("2026-10-12T14:00:00Z")

fun String.shiftedToNow(): Instant = Clock.System.now() - (DATA_CAPTURED_AT - Instant.parse(this))
