plugins {
    id("app.vercors.feature.ui")
}

dependencies {
    api(projects.feature.account.domain)
}

compose.resources {
    packageOfResClass = "app.vercors.feature.account.ui"
}
