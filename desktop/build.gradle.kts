import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") // doesn't work well in IDEA/AndroidStudio (https://github.com/JetBrains/compose-jb/issues/22)
    id("org.jetbrains.compose")
}

group = "info.anodsplace.weblists"
version = "1.0.0"

dependencies {
    implementation(project(":common"))
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("io.ktor:ktor-client-java:1.5.3")
    implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "info.anodsplace.weblists.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "info.anodsplace.weblists.desktop"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("./src/main/resources/images")
            macOS {
                iconFile.set(iconsRoot.resolve("icons-mac.icns"))
            }
//            windows {
//                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
//                menuGroup = "Web Lists"
//                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
//                upgradeUuid = "6ac4ecfc-fd32-479b-b302-40df41aa00ed"
//            }
//            linux {
//                iconFile.set(iconsRoot.resolve("ic_launcher.png"))
//            }
        }
    }
}