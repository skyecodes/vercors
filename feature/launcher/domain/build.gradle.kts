plugins {
    id("app.vercors.feature.domain")
}

dependencies {
    commonMainApi(projects.feature.instance.domain)
    commonMainApi(projects.feature.account.domain)
    commonMainApi(projects.feature.java.domain)
    commonMainApi(projects.feature.config.domain)
    commonMainApi(projects.feature.game.domain)
}
