package app.vercors

plugins {
    id("dev.zacsweers.metro")
}

metro {
    contributesAsInject.set(true)
    generateAssistedFactories.set(true)
}
