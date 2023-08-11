plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(AndroidConfig.COMPILE_JVM_VERSION)
}

dependencies {
    api(libs.kotlinx.serialization.json)
}
