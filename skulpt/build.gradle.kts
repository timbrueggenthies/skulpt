plugins {
    id("com.android.library")
    id("kotlin-android")
}

kotlin {
    explicitApi()
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        // targetSdkVersion has no effect for libraries. This is only used for the test APK
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    lint {
        textReport = true
        //textOutput = 'stdout'
        // We run a full lint analysis as build part in CI, so skip vital checks for assemble tasks
        checkReleaseBuilds = false
    }

    packagingOptions {
        // Some of the META-INF files conflict with coroutines-test. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        excludes += "/META-INF/AL2.0"
        excludes += "/META-INF/LGPL2.1"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
    }
}

dependencies {
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.runtime.runtime)
    implementation(libs.compose.material.material)
    implementation(libs.compose.animation.animation)
    implementation(libs.filament.android)
    implementation(libs.filament.utils)
    implementation(libs.filament.gltfio)
}