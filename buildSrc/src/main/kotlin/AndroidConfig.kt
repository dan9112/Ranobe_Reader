import org.gradle.api.JavaVersion

object AndroidConfig {
    const val COMPILE_SDK = 34
    const val MIN_SDK = 26
    const val TARGET_SDK = 34

    val JAVA_VERSION = JavaVersion.VERSION_17

    const val KOTLIN_COMPILER_EXTENSION_VERSION = "1.5.3"
}
