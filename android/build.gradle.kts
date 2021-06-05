plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "info.anodsplace.weblists"
        minSdk = 25
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":common"))

    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.3.0-beta01")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha02")
    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta08")
}