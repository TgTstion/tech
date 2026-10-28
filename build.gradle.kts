// Top-level build file. Versions are a known-compatible set; if Android Studio
// generated newer ones for your project, keep Studio's and only add the KSP line.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    // KSP version must start with the Kotlin version above.
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
