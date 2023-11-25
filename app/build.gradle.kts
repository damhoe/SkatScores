plugins {
    id("com.android.application")
    id("androidx.navigation.safeargs")
    kotlin("android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.damhoe.skatscores"

    defaultConfig {
        applicationId = "com.damhoe.skatscores"
        compileSdk = 34
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        resourceConfigurations += arrayOf("de","en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        dataBinding = true
    }

    kapt {
        generateStubs = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    testImplementation("junit:junit:")
    testImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.dagger:dagger:2.28.3")
    implementation("com.google.dagger:dagger-android:2.24")
    implementation("com.google.dagger:dagger-android-support:2.24")
    kapt("com.google.dagger:dagger-android-processor:2.24")
    kapt("com.google.dagger:dagger-compiler:2.28.3")
}