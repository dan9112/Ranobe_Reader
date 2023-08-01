import org.gradle.api.JavaVersion

object AndroidConfig {
    const val BASE_PACKAGE = "com.lord_markus.ranobe_reader"

    const val COMPILE_SDK = 33
    const val MIN_SDK = 24
    const val TARGET_SDK = 34

    val JAVA_VERSION = JavaVersion.VERSION_17
}
