buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val navVersion = "2.7.5"
        val kotlinVersion = "1.9.0"
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}