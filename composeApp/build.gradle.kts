import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// App iOS di SumDay con interfaccia Compose Multiplatform (stesso impianto di SkipMazeIOS):
//   desktop : prova sul PC (Gradle -> composeApp -> Tasks -> compose desktop -> run)
//   ios     : iPhone (compilata da Xcode su GitHub Actions; su Windows viene saltata)
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            // Material3: SumDay Android usa androidx.compose.material3 (Button, AlertDialog, Switch...).
            // Versione scelta dal plugin Compose Multiplatform, coerente con composeMultiplatform.
            @Suppress("DEPRECATION")
            implementation(compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.serialization.json)   // Firebase via REST (JSON)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.giammariagristina.sumday.resources"
    publicResClass = false
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "com.giammariagristina.sumday.MainKt"
    }
}
