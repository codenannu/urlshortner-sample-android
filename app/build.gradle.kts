import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.infoezeer.urlshortner"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.infoezeer.urlshortner"
        minSdk = 24
        targetSdk = 37
        // Bump versionCode for every Play Console upload (must be higher than last published).
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Replace with your shortener hosts and API base before client handoff.
        manifestPlaceholders["deepLinkHost"] = project.findProperty("DEEP_LINK_HOST") ?: "short.example.com"
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${project.findProperty("API_BASE_URL") ?: "https://api.example.com/api/v1"}\"",
        )
        buildConfigField(
            "String",
            "ALLOWED_HOSTS",
            "\"${project.findProperty("ALLOWED_HOSTS") ?: "short.example.com"}\"",
        )
        buildConfigField("long", "HTTP_TIMEOUT_MS", "15000L")
        buildConfigField("boolean", "DEBUG_LOGGING", "true")
        buildConfigField("boolean", "POST_APP_OPEN", "false")
        buildConfigField(
            "String",
            "WEB_FALLBACK_BASE_URL",
            "\"${project.findProperty("WEB_FALLBACK_BASE_URL") ?: "https://admin.example.com"}\"",
        )
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**")
            }
            buildConfigField("boolean", "DEBUG_LOGGING", "false")
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
