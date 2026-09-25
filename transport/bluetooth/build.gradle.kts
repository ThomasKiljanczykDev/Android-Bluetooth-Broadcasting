plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.bluetooth"

    defaultConfig {
        minSdk = 25
        compileSdk = 37
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":transport:core"))

    implementation(libs.androidx.coreKtx)

    implementation(libs.hilt)
    ksp(libs.hiltCompiler)
}
