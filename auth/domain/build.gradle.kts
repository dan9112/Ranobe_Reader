plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = AndroidConfig.JAVA_VERSION
    targetCompatibility = AndroidConfig.JAVA_VERSION
}
