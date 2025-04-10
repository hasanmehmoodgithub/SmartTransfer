plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.smart.transfer.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartdatatransfer.easytransfer.filetransfer.sendanydata.smartswitchmobile.copydata"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resConfigs("en", "ar", "zh", "fr", "de", "hi", "id", "it", "pt", "pl", "ru", "es", "tr", "fa", "th", "vi")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures{
        viewBinding =true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0'")
    implementation ("org.nanohttpd:nanohttpd:2.3.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation ("com.google.zxing:core:3.5.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation  ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation  ("androidx.paging:paging-runtime-ktx:3.1.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("com.github.bumptech.glide:glide:4.15.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")  // OkHttp dependency

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.airbnb.android:lottie:6.1.0") // Use the latest version
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Kotlin Extensio

    implementation ("np.com.susanthapa:curved_bottom_navigation:0.6.5")

    // Koin for Dependency Injection
    implementation ("io.insert-koin:koin-core:3.5.3")
    implementation ("io.insert-koin:koin-android:3.5.3")
    implementation ("io.insert-koin:koin-android-compat:3.5.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
