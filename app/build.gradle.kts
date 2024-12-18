plugins {
    id("com.android.application")
    kotlin("android") version "1.9.21"
    kotlin("kapt")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
}

android {
    namespace = "com.damhoe.skatscores"

    defaultConfig {
        applicationId = "com.damhoe.skatscores"
        compileSdk = 33
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
    }

    kapt {
        generateStubs = true
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }

    compileSdk = 34
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.8.3")
    implementation("androidx.navigation:navigation-ui:2.8.3")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.android.support:support-annotations:28.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Livecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Dagger
    implementation("com.google.dagger:dagger:2.49")
    implementation("com.google.dagger:dagger-android:2.49")
    implementation("com.google.dagger:dagger-android-support:2.49")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    kapt("com.google.dagger:dagger-android-processor:2.49")
    kapt("com.google.dagger:dagger-compiler:2.49")

    // Data store
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}