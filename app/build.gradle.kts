plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.app"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        manifestPlaceholders = [
//            appAuthRedirectScheme: "seniorhealthmonitoringapplication2024pvp"
//        ]
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    testImplementation("junit:junit:4.13.2")

    // https://developer.android.com/develop/ui/views/launch/splash-screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")

    // retrofit for http requests
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation ("androidx.browser:browser:1.8.0") // ... or a more recent compatible version

    // (Optional) AppAuth for OAuth simplification
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    val work_version = "2.9.0"
    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")
    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$work_version")
    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava2:$work_version")
    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:$work_version")
    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:$work_version")
    // optional - Multiprocess support
    implementation("androidx.work:work-multiprocess:$work_version")

    // Google auth stuff
    implementation("androidx.credentials:credentials:1.2.2")
    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    // coroutines alternative
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
}