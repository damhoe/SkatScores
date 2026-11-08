plugins {
    id("com.android.application") version "9.5.1" apply false
    id("com.android.legacy-kapt") version "9.5.1" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.4.21" apply false
    id("androidx.navigation.safeargs") version "2.10.2" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
