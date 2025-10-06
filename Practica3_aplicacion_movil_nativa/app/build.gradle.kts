plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.practica3_aplicacion_movil_nativa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.practica3_aplicacion_movil_nativa"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX básicos
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.core:core-ktx:1.13.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")

    // Lifecycle y Coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // CameraX
    val camerax_version = "1.3.4"
    implementation(platform("androidx.camera:camera-bom:$camerax_version"))
    implementation("androidx.camera:camera-core")
    implementation("androidx.camera:camera-camera2")
    implementation("androidx.camera:camera-lifecycle")
    implementation("androidx.camera:camera-view")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.20.0")

    // PhotoView (zoom)
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // uCrop (recorte)
    implementation("com.github.yalantis:ucrop:2.2.8")

    // GPUImage (filtros opcionales)
    // implementation("jp.co.cyberagent.android:gpuimage:2.1.0")
}
