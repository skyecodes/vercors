package app.vercors.shared.domain.model

const val DEFAULT_MIN_MEMORY_MB = 512

fun defaultMaxMemoryMb(totalMemoryMb: Int): Int = (((totalMemoryMb / 4).coerceIn(1024, 4096)) / 128) * 128
