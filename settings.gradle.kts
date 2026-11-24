pluginManagement {
    // Os convention plugins ficam num build incluído, como no Now in Android.
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "compose-notes-offline"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:testing")
include(":feature:notes")
include(":feature:editor")
