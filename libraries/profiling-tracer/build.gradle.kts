plugins {
    id("io.verse.kmm.library")
}

apply("${project.rootProject.file("gradle/github_repo_access.gradle")}")

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":libraries:profiling-core"))
            }
        }
    }
}

android {
    namespace = "io.verse.profiling.tracer"
}

pomBuilder {
    description.set("Profiling's tracer library")
}