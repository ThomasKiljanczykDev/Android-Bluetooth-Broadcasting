plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.thomas_kiljanczyk.bluetoothbroadcasting.transport"

    defaultConfig {
        minSdk = 26
        compileSdk = 37
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testFixtures {
        enable = true
    }
}

dependencies {
    api(libs.kotlinx.coroutines.android)
    api(libs.androidx.annotation)

    implementation(libs.hilt)
    ksp(libs.hiltCompiler)
}
