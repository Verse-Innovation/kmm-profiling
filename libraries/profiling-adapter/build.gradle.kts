plugins {
    id("io.verse.kmm.library")
}

apply("${project.rootProject.file("gradle/github_repo_access.gradle")}")

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":libraries:profiling-analyzer"))
                api(project(":libraries:profiling-anomaly"))
                api(project(":libraries:profiling-logger"))
                api(project(":libraries:profiling-tracer"))
            }
        }
    }
}

android {
    namespace = "io.verse.profiling.adapter"
}

pomBuilder {
    description.set("Profiling's Adapter library")
}