// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    // Multipiattaforma (modulo composeApp): dichiarati qui una volta sola,
    // cosi' tutti i plugin Kotlin stanno nello stesso classloader di Gradle (come in SkipMazeIOS).
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
