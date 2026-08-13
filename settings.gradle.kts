pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "pagecurl-cmp"

include(":pagecurl")
include(":sample:shared")
include(":sample:androidApp")
