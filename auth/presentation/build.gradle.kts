@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "${AndroidConfig.BASE_PACKAGE}.auth.presentation"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }
    kotlinOptions {
        jvmTarget = AndroidConfig.JAVA_VERSION.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation(project(":auth:domain"))
    api(project(":auth_core:presentation"))

    implementation(androidx.core.ktx)
    implementation(androidx.appcompat)
    implementation(libs.material)
    /*testImplementation(libs.junit4)
    androidTestImplementation(androidx.test.ext)
    androidTestImplementation(androidx.test.espresso.core)*/
    implementation(androidx.constraintlayout)

    implementation(compose.lifecycle.runtime)
    implementation(libs.hilt.navigation.compose)
    implementation(androidx.bundles.lifecycle)

    implementation(libs.emoji.java)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
}

kapt {
    correctErrorTypes = true
}
