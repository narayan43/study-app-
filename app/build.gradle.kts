import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.upsiprep.kxmpzq"
    minSdk = 24
    targetSdk = 35
    versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull()
      ?: (project.findProperty("appVersionCode") as String?)?.toIntOrNull()
      ?: System.getenv("VERSION_CODE")?.toIntOrNull()
      ?: 103
    versionName = (project.findProperty("versionName") as String?)
      ?: (project.findProperty("appVersionName") as String?)
      ?: System.getenv("VERSION_NAME")
      ?: "1.0.3"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val debugUploadKeyFile = rootProject.file("debug-upload.keystore")
    val debugUpload = create("debugUpload") {
      storeFile = debugUploadKeyFile
      storePassword = "android"
      keyAlias = "examprep"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
    }

    val keyPropsFile = rootProject.file("key.properties")
    val keyProps = Properties().apply {
      if (keyPropsFile.exists()) {
        load(FileInputStream(keyPropsFile))
      }
    }

    val storeFilePath = System.getenv("KEYSTORE_PATH")
      ?: keyProps.getProperty("storeFile")
      ?: if (rootProject.file("release.keystore").exists()) rootProject.file("release.keystore").absolutePath else null

    val storePasswordVal = System.getenv("STORE_PASSWORD")
      ?: System.getenv("KEYSTORE_PASSWORD")
      ?: keyProps.getProperty("storePassword")

    val keyAliasVal = System.getenv("KEY_ALIAS")
      ?: keyProps.getProperty("keyAlias")
      ?: "upload"

    val keyPasswordVal = System.getenv("KEY_PASSWORD")
      ?: keyProps.getProperty("keyPassword")
      ?: storePasswordVal

    if (storeFilePath != null && file(storeFilePath).exists() && storeFilePath != "debug-upload.keystore") {
      create("release") {
        storeFile = file(storeFilePath)
        storePassword = storePasswordVal
        keyAlias = keyAliasVal
        keyPassword = keyPasswordVal
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debugUpload")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugUpload")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation("androidx.documentfile:documentfile:1.0.1")
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
