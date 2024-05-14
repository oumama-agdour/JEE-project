plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.monumentdetection1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.monumentdetection1"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("androidx.camera:camera-view:1.3.3")

    implementation ("androidx.camera:camera-core:1.1.0-alpha05")
    implementation ("androidx.camera:camera-camera2:1.1.0-alpha05")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-alpha05")

    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    implementation ("com.google.android.material:material:<1.5.0>")

    
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}