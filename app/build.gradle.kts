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
    implementation ("androidx.camera:camera-view:1.3.4")
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")

    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("com.google.android.material:material:<1.5.0>")

    
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")
    implementation ("com.google.guava:guava:31.0.1-android")


    // Glide pour le chargement d'images
    //implementation ("com.github.bumptech.glide:glide:4.12.0")
    //implementation ("com.github.bumptech.glide:compiler:4.12.0")

    // Tensorflow lite dependencies

        implementation ("org.tensorflow:tensorflow-lite:2.5.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.0")


    implementation ("com.google.code.gson:gson:2.10.1")


}