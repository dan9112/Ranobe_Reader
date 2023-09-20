@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.com.android.library)
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
}

android {
    namespace = "${AndroidConfig.BASE_PACKAGE}.app.presentation"
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
        kotlinCompilerExtensionVersion = AndroidConfig.KOTLIN_COMPILER_EXTENSION_VERSION
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":design"))
    implementation(project(":app:domain"))

    implementation(project(":auth"))
    implementation(project(":main"))

    implementation(androidx.core.ktx)
    implementation(androidx.lifecycle.runtime.ktx)
    implementation(androidx.lifecycle.viewmodel.compose)
    implementation(compose.activity)


    implementation(androidx.navigation)

    implementation(androidx.appcompat)
    implementation(libs.material)
    /*testImplementation(libs.junit4)
    androidTestImplementation(androidx.test.ext)
    androidTestImplementation(androidx.test.espresso.core)
    androidTestImplementation(platform(compose.bom))
    androidTestImplementation(compose.ui.test.manifest)*/
    implementation(androidx.constraintlayout)

    implementation(compose.lifecycle.runtime)
    implementation(libs.hilt.navigation.compose)
    implementation(androidx.bundles.lifecycle)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
}

kapt {
    correctErrorTypes = true
}
