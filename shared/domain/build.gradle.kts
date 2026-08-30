plugins {
    id("app.vercors.shared")
    id("app.vercors.di")
}

dependencies {
    commonMainApi(projects.core.domain)
    commonMainApi(projects.core.system)
}
