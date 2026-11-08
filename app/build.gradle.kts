import java.util.Properties

plugins {
    id("com.android.application")
    id("com.android.legacy-kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("androidx.navigation.safeargs")
    id("com.google.dagger.hilt.android")
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(property: String, environmentVariable: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(environmentVariable)

android {
    namespace = "com.damhoe.skatscores"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.damhoe.skatscores"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += arrayOf("de","en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // The upload key. Its location and passwords come from keystore.properties (never
        // committed; see keystore.properties.example) or, on a build server, the environment.
        // Without either, release builds are left unsigned rather than failing.
        create("release") {
            val storePath = signingValue("storeFile", "SKATSCORES_UPLOAD_STORE_FILE")
            if (storePath != null) {
                storeFile = rootProject.file(storePath)
                storePassword = signingValue("storePassword", "SKATSCORES_UPLOAD_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "SKATSCORES_UPLOAD_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "SKATSCORES_UPLOAD_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.10.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.10.2")
    implementation("androidx.activity:activity:1.13.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.4.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    // Real SQLite on the JVM, for the migration and repository tests.
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation("androidx.test:core-ktx:1.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Livecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")

    // Dagger
    implementation("com.google.dagger:hilt-android:2.60.1")
    kapt("com.google.dagger:hilt-compiler:2.60.1")

    // Data store
    implementation("androidx.datastore:datastore-core-android:1.3.0")
    implementation("androidx.datastore:datastore-preferences:1.3.0")


    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
}
kapt {
    correctErrorTypes = true
}
