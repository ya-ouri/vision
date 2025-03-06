plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ouri.vision"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ouri.vision"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //val camerax_version = "1.3.0" // 1.3.0 1.4.0-alpha02   1.1.0-alpha06
    //implementation("androidx.camera:camera-core:${camerax_version}")
    //implementation("androidx.camera:camera-camera2:${camerax_version}")
    //implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    //implementation("androidx.camera:camera-view:${camerax_version}")


    implementation ("androidx.camera:camera-core:1.1.0-alpha06")
    implementation ("androidx.camera:camera-camera2:1.1.0-alpha06")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-alpha06")
    implementation ("androidx.camera:camera-view:1.0.0-alpha16")
    implementation("jp.co.cyberagent.android:gpuimage:2.1.0")

    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.24")
    implementation ("com.intuit.ssp:ssp-android:1.1.1")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    //implementation 'androidx.appcompat:appcompat:1.1.0'
    //implementation 'androidx.core:core-ktx:1.3.0'
    //implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    // CameraX core library using the camera2 implementation
    //val camerax_version = "1.0.0-beta06"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    //implementation ("androidx.camera:camera-core:${camerax_version}")
    //implementation ("androidx.camera:camera-camera2:${camerax_version}")
    // If you want to additionally use the CameraX Lifecycle library
    //implementation ("androidx.camera:camera-lifecycle:${camerax_version}")

    //implementation ("jp.co.cyberagent.android:gpuimage:2.1.0")

    //implementation("com.wefika:flowlayout:0.4.1")

   //testImplementation ("junit:junit:4.12")
    //androidTestImplementation ("androidx.test.ext:junit:1.1.1")
    //androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")






    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}