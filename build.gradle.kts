// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugin Android Application: Diperlukan untuk modul aplikasi
    alias(libs.plugins.android.application) apply false

    // Plugin Kotlin Android: Diperlukan untuk Kotlin. Plugin ini sudah mencakup Compose.
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}