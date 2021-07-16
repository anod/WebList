import org.jetbrains.compose.compose

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.squareup.sqldelight")
    id("kotlinx-serialization")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 25
        targetSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }

    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}

kotlin {
    android()
    jvm("desktop")

    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.materialIconsExtended)
                api(compose.ui)

                api("com.charleskorn.kaml:kaml:0.33.0")
                api("io.insert-koin:koin-core:3.0.2")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("io.ktor:ktor-client-core:1.5.4")
                implementation("com.squareup.sqldelight:runtime:1.5.0")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.0")
            }
        }
        named("androidMain") {
            dependencies {
                api("androidx.appcompat:appcompat:1.3.0")
                api("androidx.core:core-ktx:1.6.0")
                api("com.squareup.okhttp3:okhttp:4.9.0")
                api("io.ktor:ktor-client-okhttp:1.5.4")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
                implementation("org.jsoup:jsoup:1.13.1")
                implementation("com.squareup.sqldelight:android-driver:1.5.0")
                implementation("io.ktor:ktor-client-android:1.5.4")
            }
        }
        named("desktopMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.5.0")
                implementation("org.jsoup:jsoup:1.13.1")
                implementation("io.ktor:ktor-client-java:1.5.3")
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.0")
            }
        }
    }
}

sqldelight {
    database("WebListsDb") {
        packageName = "info.anodsplace.weblists.db"
    }
}
