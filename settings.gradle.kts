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

include(":launcher:gui")
include(":launcher:cli")
include(":launcher:cli:graalvm")
include(":meta:backend")
include(":meta:client")
include(":meta:lib")
include(":panel:frontend")
include(":panel:backend")
include(":shared:data")
include(":shared:domain")
include(":shared:ui")
include(":core:domain")
include(":core:data")
include(":core:ui")

include(":feature:game:data")
include(":feature:game:domain")
include(":feature:game:ui")

include(":feature:project:data")
include(":feature:project:domain")
include(":feature:project:ui")

include(":feature:instance:data")
include(":feature:instance:domain")
include(":feature:instance:ui")

include(":feature:home:data")
include(":feature:home:domain")
include(":feature:home:ui")

include(":feature:account:data")
include(":feature:account:domain")
include(":feature:account:ui")

include(":feature:launcher:data")
include(":feature:launcher:domain")
include(":feature:launcher:ui")

include(":feature:config:data")
include(":feature:config:domain")
include(":feature:config:ui")

include(":feature:config-gui:data")
include(":feature:config-gui:domain")
include(":feature:config-gui:ui")
