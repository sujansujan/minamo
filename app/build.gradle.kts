plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val ciRunNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
val computedVersionCode = if (ciRunNumber != null) 10 + ciRunNumber else 8

val releaseKeystorePath = System.getenv("MINAMO_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("MINAMO_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("MINAMO_KEY_ALIAS")
val releaseKeyPassword = System.getenv("MINAMO_KEY_PASSWORD")
val hasPersistentReleaseKey =
    !releaseKeystorePath.isNullOrBlank() &&
        !releaseKeystorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank() &&
        file(releaseKeystorePath).exists()

android {
    namespace = "com.example.koipond"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.koipond"
        minSdk = 26
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = "2.9.0"
    }

    signingConfigs {
        if (hasPersistentReleaseKey) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasPersistentReleaseKey) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
