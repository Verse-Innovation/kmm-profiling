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
    namespace = "io.verse.profiling.logger"
}

pomBuilder {
    description.set("Profiling's logger library")
}