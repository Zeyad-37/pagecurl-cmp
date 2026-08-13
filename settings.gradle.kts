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

rootProject.name = "PageCurl"

include(":pagecurl")
include(":sample:shared")
include(":sample:androidApp")
// The legacy Android-only demo (`:demo`) is kept in the tree for reference but is no
// longer part of the build — the multiplatform sample under `sample/` replaces it.
