plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "kivaa.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "kivaa.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // --- Product Flavors Configuration ---
    flavorDimensions += "platform"

    productFlavors {
        create("factoryFuture") {
            dimension = "platform"
            applicationId = "kivaa.factoryfuture"
            versionName = "1.0-ff"
            buildConfigField("String", "PLATFORM_KEY", "\"kivaa_factory_future_mobile_Kt9AKaR7Q3pJ_Y9WO1NxOogvE6nTnhbj\"")
            manifestPlaceholders["appLabel"] = "Factory Future"
        }
        create("theIndustrial") {
            dimension = "platform"
            applicationId = "kivaa.theindustrial"
            versionName = "1.0-ti"
            buildConfigField("String", "PLATFORM_KEY", "\"kivaa_the_industrial_mobile_QF9PLdi9smCZbrLaDLTX-6t7t-EReE1S\"")
            manifestPlaceholders["appLabel"] = "The Industrial"
        }
        create("thingsOfBusiness") {
            dimension = "platform"
            applicationId = "kivaa.thingsofbusiness"
            versionName = "1.0-tob"
            buildConfigField("String", "PLATFORM_KEY", "\"kivaa_things_of_business_mobile_-IHdHKSI-2OHY7HdAQC8qJFlY8ryMmDA\"")
            manifestPlaceholders["appLabel"] = "Things of Business"
        }
        create("mobilityHyperdrive") {
            dimension = "platform"
            applicationId = "kivaa.mobility"
            versionName = "1.0-mh"
            buildConfigField("String", "PLATFORM_KEY", "\"kivaa_mobility_hyperdrive_mobile_S0yAFbkK2KozdvJzBrbwXeSdU1Nr0OKs\"")
            manifestPlaceholders["appLabel"] = "Mobility Hyperdrive"
        }
        create("bankingOnTech") {
            dimension = "platform"
            applicationId = "kivaa.banking"
            versionName = "1.0-bot"
            buildConfigField("String", "PLATFORM_KEY", "\"kivaa_banking_on_technology_mobile_oTZ8lQ3h_tmdaLO93IogxcBypyeylHBH\"")
            manifestPlaceholders["appLabel"] = "Banking on Technology"
        }
        create("technologue") {
            dimension = "platform"
            applicationId = "kivaa.technologue"
            versionName = "1.0-tech"
            buildConfigField("String", "PLATFORM_KEY", "\"kivaa_technologue_mobile_BJyOcCk7zUpTbKVVCLvvZRJKL7U9paxv\"")
            manifestPlaceholders["appLabel"] = "Technologue"
        }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.material)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore.preferences)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
