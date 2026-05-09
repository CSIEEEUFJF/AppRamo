plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.ramoieeeufjf.appRamo"
    compileSdk {
        version = release(36)
    }

    val doorRelayBaseUrl = providers.gradleProperty("DOOR_RELAY_BASE_URL")
        .orElse(providers.environmentVariable("DOOR_RELAY_BASE_URL"))
        .orElse("https://ramoieeeufjf.dpdns.org")
    val doorRelayApiKey = providers.gradleProperty("DOOR_RELAY_API_KEY")
        .orElse(providers.environmentVariable("DOOR_RELAY_API_KEY"))
        .orElse("")
    val doorRelayDeviceId = providers.gradleProperty("DOOR_RELAY_DEVICE_ID")
        .orElse(providers.environmentVariable("DOOR_RELAY_DEVICE_ID"))
        .orElse("esp01")
    fun String.asBuildConfigString(): String = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

    defaultConfig {
        applicationId = "com.ramoieeeufjf.appRamo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DOOR_RELAY_BASE_URL", doorRelayBaseUrl.get().asBuildConfigString())
        buildConfigField("String", "DOOR_RELAY_API_KEY", doorRelayApiKey.get().asBuildConfigString())
        buildConfigField("String", "DOOR_RELAY_DEVICE_ID", doorRelayDeviceId.get().asBuildConfigString())
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
