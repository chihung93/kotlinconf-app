import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

val kotlin_version: String by project
val anko_version: String by project
val coroutines_version: String by project
val ktor_version: String by project
val sticky_headers: String by project
val glide_version: String by project

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlinx-serialization")
}

android {
    compileSdkVersion(28)
    buildToolsVersion = "29.0.1"
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

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.navigation:navigation-fragment:2.1.0")
    implementation("androidx.navigation:navigation-ui:2.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.1.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.1.0")

    implementation("com.android.support:multidex:1.0.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("io.ktor:ktor-client-android:$ktor_version")

    implementation("com.mapbox.mapboxsdk:mapbox-android-sdk:8.3.0")

    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
