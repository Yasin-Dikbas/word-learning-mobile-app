plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.nisanurguven.wordleloop"
    compileSdk = 36 // Hatalı blok temizlendi, stabil sürüme sabitlendi

    defaultConfig {
        applicationId = "com.nisanurguven.wordleloop"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties içindeki API anahtarını güvenli ve hatasız çekme yöntemi
        val apiKey = project.findProperty("GEMINI_API_KEY") as? String ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
    }

    // Dağınık olan özellikler tek bir çatı altında toplandı
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Projene özel kütüphaneler
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation("io.coil-kt:coil:2.4.0")
    implementation("com.github.yuyakaido:CardStackView:2.3.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
}