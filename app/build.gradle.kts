plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    val major = 0
    val minor = 1
    val patch = 0

    defaultConfig {
        applicationId = "dev.thomas_kiljanczyk.bluetoothbroadcasting"
        minSdk = 25
        compileSdk = 36
        targetSdk = 37
        versionCode = major * 100000000 + minor * 10000 + patch
        versionName = "$major.$minor.$patch"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "dev.thomas_kiljanczyk.bluetoothbroadcasting"
}

dependencies {
    // App dependencies
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.services.nearby)

    // AndroidX
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.annotation)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    // Hilt
    implementation(libs.hilt)
    ksp(libs.hiltCompiler)

    // LeakCanary
//    debugImplementation "com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion"
}
