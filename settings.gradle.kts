pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "profiling"
include(":applications:the101:android")

include(":libraries:profiling-core")
include(":libraries:profiling-tracer")
include(":libraries:profiling-analyzer")
include(":libraries:profiling-logger")
include(":libraries:profiling-anomaly")
include(":libraries:profiling-adapter")
include(":libraries:profiling-android")
