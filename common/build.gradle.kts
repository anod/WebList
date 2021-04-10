import org.jetbrains.compose.compose

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.squareup.sqldelight")
    id("kotlinx-serialization")
}
//
//android {
//    configurations {
////        create("androidTestApi")
////        create("androidTestDebugApi")
////        create("androidTestReleaseApi")
//        create("testApi")
//        create("testDebugApi")
//        create("testReleaseApi")
//    }
//}

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

                api("com.charleskorn.kaml:kaml:0.29.0")
                api("io.insert-koin:koin-core:3.0.1-beta-2")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                implementation("io.ktor:ktor-client-core:1.5.3")
                implementation("com.squareup.sqldelight:runtime:1.4.4")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.4.4")
            }
        }
        named("androidMain") {
            dependencies {
                api("androidx.appcompat:appcompat:1.3.0-rc01")
                api("androidx.core:core-ktx:1.3.2")

                implementation("org.jsoup:jsoup:1.13.1")
                implementation("com.squareup.sqldelight:android-driver:1.4.4")
                implementation("io.ktor:ktor-client-android:1.5.3")
            }
        }
        named("desktopMain") {
            dependencies {
                implementation("org.jsoup:jsoup:1.13.1")
                implementation("io.ktor:ktor-client-java:1.5.3")
                implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
            }
        }
    }
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(25)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }

//    lintOptions {
//        isAbortOnError = false
//    }
}

sqldelight {
    database("WebListsDb") {
        packageName = "info.anodsplace.weblists.db"
    }
}
