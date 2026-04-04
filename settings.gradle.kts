pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "garden log"
include(":app")
include(":data")
include(":domain")
include(":feature:plant")
include(":feature:setting")
include(":core:database")
include(":core:remote")
include(":core:file")
include(":core:common_ui")
include(":core:analytics")
include(":core:pdf")
