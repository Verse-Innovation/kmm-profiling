plugins {
    id("io.verse.kmm.library")
}

apply("${project.rootProject.file("gradle/github_repo_access.gradle")}")

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.tagd.arch)
                api(libs.verse.app.bundle)
                api(libs.verse.latch)
                api(libs.verse.storage)
            }
        }
    }
}

android {
    namespace = "io.verse.profiling.core"
}

pomBuilder {
    description.set("Profiling core library")
}