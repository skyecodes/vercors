plugins {
    id("app.vercors.feature.command")
}

dependencies {
    commonMainImplementation(projects.feature.game.domain)
}
