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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SumDayIOS"
// app: l'app Android di SumDay (copia del progetto AndroidStudioProjects/Sumday del 20/10/2026).
// Su GitHub Actions (variabile SOLO_IOS) si compila solo la parte iOS: l'app Android e' esclusa.
if (System.getenv("SOLO_IOS") == null) include(":app")
// composeApp: app iPhone (Compose Multiplatform). SumDay non ha un modulo motore separato:
// MagicSquareEngine sta in app/ e la sua copia multipiattaforma andra' in composeApp/commonMain.
include(":composeApp")
