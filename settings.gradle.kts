rootProject.name = "vercors"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":launcher:cli")
include(":launcher:cli:graalvm")
include(":launcher:gui")

include(":meta:backend")
include(":meta:client")
include(":meta:lib")

include(":core:domain")
include(":core:data")
include(":core:ui")

include(":shared:command")
include(":shared:data")
include(":shared:domain")
include(":shared:ui")
include(":shared:ui:screen")

include(":feature:game:command")
include(":feature:game:data")
include(":feature:game:domain")
include(":feature:game:ui")
include(":feature:game:ui:screen")

include(":feature:project:command")
include(":feature:project:data")
include(":feature:project:domain")
include(":feature:project:ui")
include(":feature:project:ui:screen")

include(":feature:instance:command")
include(":feature:instance:data")
include(":feature:instance:domain")
include(":feature:instance:ui")
include(":feature:instance:ui:screen")

include(":feature:home:command")
include(":feature:home:data")
include(":feature:home:domain")
include(":feature:home:ui")
include(":feature:home:ui:screen")

include(":feature:account:command")
include(":feature:account:data")
include(":feature:account:domain")
include(":feature:account:ui")
include(":feature:account:ui:screen")

include(":feature:launcher:command")
include(":feature:launcher:data")
include(":feature:launcher:domain")
include(":feature:launcher:ui")
include(":feature:launcher:ui:screen")

include(":feature:config:command")
include(":feature:config:data")
include(":feature:config:domain")
include(":feature:config:ui")
include(":feature:config:ui:screen")

include(":feature:config-gui:command")
include(":feature:config-gui:data")
include(":feature:config-gui:domain")
include(":feature:config-gui:ui")
include(":feature:config-gui:ui:screen")
