@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("kotlinx") {
            from(files("gradle/kotlinx.versions.toml"))
        }
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
        create("compose") {
            from(files("gradle/compose.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Ranobe Reader"
include(":app")
include(":core")
include(":design")
include(":data")
include(":auth_core", ":auth_core:presentation", ":auth_core:domain")
include(":auth", ":auth:presentation", ":auth:domain")
include(":main", ":main:presentation", ":main:domain")
include(":home")
include(":settings")
include(":history")
include(":recommendations")
include(":my_shelf")
 