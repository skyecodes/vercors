package app.vercors.shared.domain

data class Page<T>(
    val items: List<T>,
    val total: Int
)

fun <T, U> Page<T>.map(transform: (T) -> U): Page<U> = Page(items.map(transform), total)
