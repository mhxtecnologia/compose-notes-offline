dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    // Reaproveita o mesmo catálogo de versões do projeto principal.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
