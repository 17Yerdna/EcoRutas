plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "pe.upeu.ecorecicla"
    compileSdk = 36
    defaultConfig { applicationId = "pe.upeu.ecorecicla"; minSdk = 24; targetSdk = 36; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
    buildFeatures { compose = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies { implementation(project(":shared")); implementation("androidx.activity:activity-compose:1.13.0"); implementation("io.insert-koin:koin-core:4.0.2") }
