// File: <root>/build.gradle.kts

plugins {
    // Note: “apply false” means these plugins are available to subprojects, but not applied here.
    id("com.android.application") version "8.8.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
