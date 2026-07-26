import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "kivaa.app"
    compileSdk = 37

    // Load keys from local.properties
    val props = Properties().apply {
        val propertiesFile = rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use { load(it) }
        }
    }

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
            applicationId = "kivaa.app.factoryfuture"
            versionName = "1.0-ff"
            buildConfigField("String", "PLATFORM_KEY", "\"${props.getProperty("PLATFORM_KEY_FACTORY_FUTURE", "")}\"")
            manifestPlaceholders["appLabel"] = "Factory Future"
            manifestPlaceholders["appHost"] = "www.factoryfuture.in"
            manifestPlaceholders["appHostShort"] = "factoryfuture.in"
        }
        create("theIndustrial") {
            dimension = "platform"
            applicationId = "kivaa.app.theindustrial"
            versionName = "1.0-ti"
            buildConfigField("String", "PLATFORM_KEY", "\"${props.getProperty("PLATFORM_KEY_THE_INDUSTRIAL", "")}\"")
            manifestPlaceholders["appLabel"] = "The Industrial"
            manifestPlaceholders["appHost"] = "www.theindustrial.in"
            manifestPlaceholders["appHostShort"] = "theindustrial.in"
        }
        create("thingsOfBusiness") {
            dimension = "platform"
            applicationId = "kivaa.app.thingsofbusiness"
            versionName = "1.0-tob"
            buildConfigField("String", "PLATFORM_KEY", "\"${props.getProperty("PLATFORM_KEY_THINGS_OF_BUSINESS", "")}\"")
            manifestPlaceholders["appLabel"] = "Things of Business"
            manifestPlaceholders["appHost"] = "www.thingsofbusiness.com"
            manifestPlaceholders["appHostShort"] = "thingsofbusiness.com"
        }
        create("mobilityHyperdrive") {
            dimension = "platform"
            applicationId = "kivaa.app.mobility"
            versionName = "1.0-mh"
            buildConfigField("String", "PLATFORM_KEY", "\"${props.getProperty("PLATFORM_KEY_MOBILITY_HYPERDRIVE", "")}\"")
            manifestPlaceholders["appLabel"] = "Mobility Hyperdrive"
            manifestPlaceholders["appHost"] = "www.mobilityhyperdrive.in"
            manifestPlaceholders["appHostShort"] = "mobilityhyperdrive.in"
        }
        create("bankingOnTech") {
            dimension = "platform"
            applicationId = "kivaa.app.banking"
            versionName = "1.0-bot"
            buildConfigField("String", "PLATFORM_KEY", "\"${props.getProperty("PLATFORM_KEY_BANKING_ON_TECH", "")}\"")
            manifestPlaceholders["appLabel"] = "Banking on Technology"
            manifestPlaceholders["appHost"] = "www.bankingontechnology.com"
            manifestPlaceholders["appHostShort"] = "bankingontechnology.com"
        }
        create("technologue") {
            dimension = "platform"
            applicationId = "kivaa.app.technologue"
            versionName = "1.0-tech"
            buildConfigField("String", "PLATFORM_KEY", "\"${props.getProperty("PLATFORM_KEY_TECHNOLOGUE", "")}\"")
            manifestPlaceholders["appLabel"] = "Technologue"
            manifestPlaceholders["appHost"] = "www.technologue.in"
            manifestPlaceholders["appHostShort"] = "technologue.in"
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
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

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
