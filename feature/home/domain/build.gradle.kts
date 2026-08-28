plugins {
    id("app.vercors.feature.domain")
}

dependencies {
    commonMainApi(projects.feature.project.domain)
    commonMainApi(projects.feature.instance.domain)
    commonMainApi(projects.feature.config.domain)
}
