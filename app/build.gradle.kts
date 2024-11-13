plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.provaprogetto"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.provaprogetto"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        create("customDebugType") {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding= true

    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity.ktx.v191)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.ui.desktop)
    implementation(libs.androidx.monitor)
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx.v270)
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v270)

    //room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    implementation(libs.moshi.kotlin)

    // Retrofit with Moshi Converter
    implementation(libs.converter.moshi)
    implementation(libs.retrofit.v2110)

    // Coil
    implementation(libs.coil)
    implementation (libs.picasso)
    implementation(libs.gson)

    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.database)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    //cloud firestore
    implementation(libs.firebase.firestore)

    implementation(libs.play.services.basement)
    // firebase
    implementation(libs.google.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.kotlin.stdlib)

    //test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.robolectric)

    // Mockito
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

}