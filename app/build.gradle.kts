plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "tv.alkafel.live"
    compileSdk = 35

    defaultConfig {
        applicationId = "tv.alkafel.live"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "3.0-iraq"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("ALKAFEL_KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("ALKAFEL_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ALKAFEL_KEY_ALIAS")
                keyPassword = System.getenv("ALKAFEL_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
}
