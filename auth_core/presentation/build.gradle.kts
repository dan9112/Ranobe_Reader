@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "${AndroidConfig.BASE_PACKAGE}.auth_core.presentation"
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }
    kotlinOptions {
        jvmTarget = AndroidConfig.JAVA_VERSION.toString()
    }
}

dependencies {
    api(project(":design"))

    api(project(":auth:domain"))

    api(compose.bom)
    api(compose.bundles.ui)
    api(compose.material3)
    debugApi(compose.ui.tooling)
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