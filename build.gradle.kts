buildscript {
    val composeVersion = "0.5.0-build245"

    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath("org.jetbrains.compose:compose-gradle-plugin:$composeVersion")
        classpath("com.android.tools.build:gradle:7.0.0-beta05")
        classpath(kotlin("gradle-plugin", version = "1.5.10"))
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.10")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}