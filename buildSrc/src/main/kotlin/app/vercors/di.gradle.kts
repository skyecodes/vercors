package app.vercors

plugins {
    id("app.vercors.base")
    id("dev.zacsweers.metro")
}

metro {
    debug = true
    contributesAsInject.set(true)
}
