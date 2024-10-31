import app.vercors.launcher.build.*

plugins {
    id("app.vercors.launcher.presentation")
}

dependencies {
    moduleImpl(Core.presentation)
    moduleImpl(Home.domain)
    moduleImpl(Instance.presentation)
    moduleImpl(Instance.domain)
    moduleImpl(Project.presentation)
    moduleImpl(Project.domain)
}