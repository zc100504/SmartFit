plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KAPT is needed for Room compiler
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.smartfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartfit"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 这里新增这一段：
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // JDK 17 recommended for Compose + Room + Coroutines
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    //-- UI Material
    implementation("androidx.compose.material3:material3-window-size-class:1.3.0")
    implementation("com.google.android.material:material:1.12.0")



    implementation("androidx.activity:activity-ktx:1.9.2")

    //--- System UI Controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // --- Base Jetpack / Compose (from version catalog) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // --- Lifecycle & ViewModel ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")


    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Room (database) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.animation)
    implementation(libs.foundation)
    kapt("androidx.room:room-compiler:2.6.1")

    // --- Retrofit + Moshi (network / JSON) ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Coil (image loading for Compose) ---
    implementation("io.coil-kt:coil-compose:2.7.0")

    // --- DataStore (Preferences) ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- Health Connect ---
    implementation("androidx.health.connect:connect-client:1.2.0-alpha01")

    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("io.mockk:mockk-android:1.13.10")

}
