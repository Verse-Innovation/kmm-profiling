plugins {
    id("com.android.application")
    kotlin("android")
}

apply("${project.rootProject.file("gradle/secrets.gradle")}")

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")

    maven {
        url = uri("https://maven.pkg.github.com/pavan2you/kmm-clean-architecture")

        credentials {
            username = extra["githubUser"] as? String
            password = extra["githubToken"] as? String
        }
    }
}

android {
    namespace = "io.verse.profiling.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "io.verse.profiling.android"
        minSdk = 21
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"1\"")
        buildConfigField("int", "VERSION_CODE", "1")
        buildConfigField("String", "SCHEME", "\"https\"")
        buildConfigField("String", "BASE_URL", "\"https://demo2921399.mockable.io\"")

        // Deeplink configuration
        val appScheme = "sample"
        val appHost = "sampleApp"
        val firebaseDynamicLinkDomain = "apps.sampleApp.com"
        val deepLinkDomain = "www.sampleApp.com"
        buildConfigField("String", "APP_SCHEME", "\"$appScheme://\"")
        buildConfigField("String", "APP_HOST", "\"$appHost\"")
        buildConfigField("String", "FIREBASE_DYNAMIC_LINK_DOMAIN", "\"$firebaseDynamicLinkDomain\"")
        buildConfigField("String", "DEEP_LINK_DOMAIN", "\"$deepLinkDomain\"")
    }
    flavorDimensions += listOf("free", "paid")
    productFlavors {
        create("demo") {
            dimension = "free"
            buildConfigField("String", "FLAVOUR_DIMENSION", "\"free\"")
        }
        create("pro") {
            dimension = "paid"
            buildConfigField("String", "FLAVOUR_DIMENSION", "\"paid\"")
        }
    }
}

dependencies {

    implementation(project(":libraries:profiling-android"))
    implementation(libs.tagd.android)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.activity.compose)
}