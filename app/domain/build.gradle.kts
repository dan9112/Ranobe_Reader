plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("kotlin-kapt")
}

kotlin {
    jvmToolchain(AndroidConfig.COMPILE_JVM_VERSION)
}

dependencies {
    api(project(":core"))

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(kotlinx.coroutines.core)
}
