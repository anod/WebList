import org.jetbrains.compose.compose

plugins {
    kotlin("jvm") // doesn't work well in IDEA/AndroidStudio (https://github.com/JetBrains/compose-jb/issues/22)
    id("org.jetbrains.compose")
}

group = "info.anodsplace.weblists"
version = "1.0.0"

dependencies {
    implementation(project(":common"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "info.anodsplace.weblists.desktop.MainKt"

//        nativeDistributions {
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
//            packageName = "info.anodsplace.weblists.desktop"
//            packageVersion = "1.0.0"
//        }
    }
}