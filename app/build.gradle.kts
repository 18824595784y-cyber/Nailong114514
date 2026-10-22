plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
android {
 namespace = "cn.minimal.todo"
 compileSdk = 34
 defaultConfig { applicationId = "cn.minimal.todo"; minSdk = 26; targetSdk = 34; versionCode = 2; versionName = "1.1" }
 signingConfigs {
  create("publicRelease") {
   val path = System.getenv("TODO_KEYSTORE")
   if (!path.isNullOrBlank()) {
    storeFile = file(path)
    storePassword = System.getenv("TODO_STORE_PASSWORD")
    keyAlias = System.getenv("TODO_KEY_ALIAS")
    keyPassword = System.getenv("TODO_KEY_PASSWORD")
   }
  }
 }
 buildTypes { getByName("release") { if (!System.getenv("TODO_KEYSTORE").isNullOrBlank()) signingConfig = signingConfigs.getByName("publicRelease") } }
 buildFeatures { compose = true }
 composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation("androidx.activity:activity-compose:1.9.0")
 implementation("androidx.compose.material3:material3:1.2.1")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
 implementation("androidx.room:room-runtime:2.6.1")
 implementation("androidx.room:room-ktx:2.6.1")
 kapt("androidx.room:room-compiler:2.6.1")
}


