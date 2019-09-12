import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

val kotlin_version: String by project
val anko_version: String by project
val coroutines_version: String by project
val ktor_version: String by project
val sticky_headers: String by project
val glide_version: String by project
val androidx_base: String by project
val androidx_ui: String by project
val androidx_navigation: String by project
val android_multidex: String by project
val android_material: String by project
val android_constraint_layout: String by project
val android_mapbox: String by project
val junit_version: String by project
val androidx_test: String by project
val androidx_espresso: String by project

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlinx-serialization")
}

android {
    compileSdkVersion(28)
    buildToolsVersion = "29.0.2"
    defaultConfig {
        applicationId = "com.jetbrains.kotlinconf"
        minSdkVersion(16)
        targetSdkVersion(28)
        multiDexEnabled = true
        versionCode = 10
        versionName = "1.0.9"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        val release by getting {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
    kotlinOptions {
        val options = this as KotlinJvmOptions
        options.jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":common"))

    implementation("androidx.appcompat:appcompat:$$androidx_base")
    implementation("androidx.core:core-ktx:$androidx_base")
    implementation("androidx.vectordrawable:vectordrawable:$androidx_base")

    implementation("androidx.navigation:navigation-fragment:$androidx_ui")
    implementation("androidx.navigation:navigation-ui:$androidx_ui")
    implementation("androidx.lifecycle:lifecycle-extensions:$androidx_ui")
    implementation("androidx.navigation:navigation-fragment-ktx:$androidx_ui")
    implementation("androidx.navigation:navigation-ui-ktx:$androidx_ui")

    implementation("com.google.android.material:material:$android_material")
    implementation("androidx.constraintlayout:constraintlayout:$android_constraint_layout")

    implementation("com.android.support:multidex:$android_multidex")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("io.ktor:ktor-client-android:$ktor_version")

    implementation("com.mapbox.mapboxsdk:mapbox-android-sdk:$android_mapbox")

    testImplementation("junit:junit:$junit_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")

    androidTestImplementation("androidx.test:runner:$androidx_test")
}
