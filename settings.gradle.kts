pluginManagement {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://jitpack.io")  }
    }
}

rootProject.name = "Oxygen Customizer"
include(":app")
include(":oos_launcher")
include(":oos_systemui")
