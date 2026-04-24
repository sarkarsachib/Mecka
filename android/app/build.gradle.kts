plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "ai.mecka"
    compileSdk = 34

    defaultConfig {
        applicationId = "ai.mecka"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        create("devMode") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("boolean", "DEV_MODE", "true")
            buildConfigField("boolean", "ROOT_ACCESS", "false")
        }

        create("rootAccess") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".root"
            versionNameSuffix = "-root"
            buildConfigField("boolean", "DEV_MODE", "true")
            buildConfigField("boolean", "ROOT_ACCESS", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlinx.coroutines.FlowPreview"
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/override-release.kotlin_module"
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("standard") {
            dimension = "mode"
            buildConfigField("boolean", "FEATURE_FULL_ACCESSIBILITY", "true")
            buildConfigField("boolean", "FEATURE_BLUETOOTH_EARBUD", "true")
            buildConfigField("boolean", "FEATURE_NOTIFICATION_LISTENER", "true")
            buildConfigField("boolean", "FEATURE_ADVANCED_SECURITY", "true")
        }

        create("limited") {
            dimension = "mode"
            buildConfigField("boolean", "FEATURE_FULL_ACCESSIBILITY", "false")
            buildConfigField("boolean", "FEATURE_BLUETOOTH_EARBUD", "true")
            buildConfigField("boolean", "FEATURE_NOTIFICATION_LISTENER", "false")
            buildConfigField("boolean", "FEATURE_ADVANCED_SECURITY", "false")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // AndroidX
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Bluetooth
    implementation("androidx.bluetooth:bluetooth:1.0.0-alpha02")

    // Logging
    implementation("com.github.ajalt:timberkt:1.5.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Mockito
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    androidTestImplementation("org.mockito:mockito-android:5.11.0")
}