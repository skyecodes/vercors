plugins {
    id("app.vercors.feature.domain")
}

dependencies {
    commonMainApi(projects.shared.domain)
    commonMainApi(projects.feature.instance.domain)
    commonMainApi(projects.feature.account.domain)
    commonMainApi(projects.feature.java.domain)
}
