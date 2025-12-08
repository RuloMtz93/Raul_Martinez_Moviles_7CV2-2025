plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // 👇 NUEVO: plugin requerido con Kotlin 2.x
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"

    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.conecta4"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.conecta4"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    // ⛔️ Quita esto en Kotlin 2.x (ya no se usa):
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "..."
    // }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // BOM de Compose (elige una estable reciente)
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("com.google.android.material:material:1.12.0")

    //Navegación
    implementation("androidx.navigation:navigation-compose:2.8.3")

    //Icons
    implementation("androidx.compose.material:material-icons-extended")


    debugImplementation("androidx.compose.ui:ui-tooling")

    // JSON (kotlinx.serialization)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

// Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")

}
