import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.service)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.mskwak.gardendailylog"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }
    defaultConfig {
        applicationId = "com.mskwak.gardendailylog"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 7
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "${rootProject.name} $versionName($versionCode)")
    }
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":design"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":feature:plant"))
    implementation(project(":core:common_ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.timber)
    debugImplementation(libs.bundles.debug.compose)
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlin.serialization)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.androidTest)
    androidTestImplementation(platform(libs.compose.bom))
    debugImplementation(libs.bundles.debug.compose)
}