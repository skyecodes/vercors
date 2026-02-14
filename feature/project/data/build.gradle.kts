plugins {
    id("app.vercors.feature.data")
}

dependencies {
    commonMainApi(projects.feature.project.domain)
    commonMainApi(projects.feature.project.api)
}
