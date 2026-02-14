plugins {
    id("app.vercors.feature.domain")
}

dependencies {
    commonMainImplementation(projects.feature.project.domain)
    commonMainImplementation(projects.feature.instance.domain)
    commonMainImplementation(projects.feature.config.domain)
}
