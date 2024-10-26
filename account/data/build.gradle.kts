import app.vercors.launcher.build.Account
import app.vercors.launcher.build.Core
import app.vercors.launcher.build.moduleImpl

plugins {
    id("app.vercors.launcher.data")
}

dependencies {
    moduleImpl(Core.data)
    moduleImpl(Account.domain)
}