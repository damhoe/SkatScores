plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.21" apply false
    id("androidx.navigation.safeargs") version "2.9.6" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
