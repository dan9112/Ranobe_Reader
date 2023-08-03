plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

kotlin {
    jvmToolchain(AndroidConfig.COMPILE_JVM_VERSION)
}
