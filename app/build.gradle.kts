import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val googleWebClientId = localProperties.getProperty("googleWebClientId") ?: ""
val backendBaseUrl = localProperties.getProperty("backendBaseUrl") ?: "https://your-backend-url.com"

android {
    namespace = "com.example.movieswipe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.movieswipe"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
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
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("generateNetworkSecurityConfig") {
    val backendBaseUrl = localProperties.getProperty("backendBaseUrl") ?: "http://localhost"
    val domain = backendBaseUrl
        .removePrefix("http://")
        .removePrefix("https://")
        .substringBefore(":")
        .substringBefore("/")
    val xmlContent = """
        <?xml version="1.0" encoding="utf-8"?>
        <network-security-config>
            <domain-config cleartextTrafficPermitted="true">
                <domain includeSubdomains="false">$domain</domain>
            </domain-config>
        </network-security-config>
    """.trimIndent()
    val outputDir = file("src/main/res/xml")
    outputs.file(outputDir.resolve("network_security_config.xml"))
    doLast {
        outputDir.mkdirs()
        outputDir.resolve("network_security_config.xml").writeText(xmlContent)
    }
}

tasks.named("preBuild") {
    dependsOn("generateNetworkSecurityConfig")
}
