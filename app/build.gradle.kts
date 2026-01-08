plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "de.lobianco.pr0gameunofficial"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "de.lobianco.pr0gameunofficial"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.3"

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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
// ViewPager2 für Planet-Swipe
    implementation("androidx.viewpager2:viewpager2:1.1.0")
// SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
// Fragment
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("com.google.code.gson:gson:2.13.2")


}