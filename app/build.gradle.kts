import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val oauthProperties = Properties().apply {
    val credentialsFile = rootProject.file("oauth.properties")
    if (credentialsFile.isFile) credentialsFile.inputStream().use { load(it) }
}
fun oauthValue(key: String): String = oauthProperties.getProperty(key, "")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\r", "\\r")
    .replace("\n", "\\n")

android {
    namespace = "com.example.tumblrlite"
    compileSdk = 37
    defaultConfig {
        buildConfigField("String", "TUMBLR_CONSUMER_KEY", "\"${oauthValue("consumerKey")}\"")
        buildConfigField("String", "TUMBLR_CONSUMER_SECRET", "\"${oauthValue("consumerSecret")}\"")
        buildConfigField("String", "TUMBLR_OAUTH_TOKEN", "\"${oauthValue("oauthToken")}\"")
        buildConfigField("String", "TUMBLR_OAUTH_TOKEN_SECRET", "\"${oauthValue("oauthTokenSecret")}\"")
        applicationId = "com.example.tumblrlite"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.paging.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
