plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.audivue.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.audivue.sample"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":video-sdk"))
    implementation("androidx.appcompat:appcompat:1.7.0")
}
