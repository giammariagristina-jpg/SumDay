plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.giammariagristina.sumday"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.giammariagristina.sumday"
        minSdk = 26
        targetSdk = 36
        // [20/10/2026] Classifica di test o di produzione: stesso meccanismo di SkipMaze e Astro Tilt.
        // ─── PROCEDURA OPERATIVA PER IL RILASCIO IN PRODUZIONE ───────────────────
        // 1. Il versionCode di test è sempre +1 rispetto all'ultimo rilascio in produzione.
        // 2. INTERNAL_PROD_CODE_VERSION è normalmente = 0 (build di test/sviluppo).
        // 3. Prima del rilascio in produzione:
        //      a. Impostare INTERNAL_PROD_CODE_VERSION = versionCode che si sta rilasciando.
        //      b. Verificare i valori, compilare e generare l'AAB firmato.
        //      c. Caricare l'AAB su Play Console e pubblicare.
        // 4. Dopo il rilascio:
        //      a. Incrementare versionCode di +1.
        //      b. Aggiornare versionName.
        //      c. Azzerare INTERNAL_PROD_CODE_VERSION a 0.
        // ─────────────────────────────────────────────────────────────────────────
        // PRODUZIONE CURRENT  nessuna (SumDay non e' ancora pubblicata)
        versionCode = 5
        versionName = "4.0.0"
        // ATTENZIONE - PRIMA DI PASSAGGIO IN PRODUZIONE METTERE = CODEVERSION
        buildConfigField("int", "INTERNAL_PROD_CODE_VERSION", "0")
        //ATTENZIONE-> DOPO PASSAGGIO IN PRODUZIONE METTERE 0

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true   // [20/10/2026] per INTERNAL_PROD_CODE_VERSION
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.config)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
