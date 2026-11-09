package com.example.tech.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Target accent: drives the YouGlish URL and the TextToSpeech voice. */
enum class Accent(val label: String, val youglishCode: String, val locale: Locale) {
    UK("UK", "uk", Locale.UK),
    US("US", "us", Locale.US),
    AUS("Australian", "aus", Locale("en", "AU")),
}

/**
 * Settings shown on Screen 3. Milestone 1 keeps them in memory (Compose state, shared by
 * all Activities in the process); Milestone 2 will persist them with Jetpack DataStore.
 */
object AppSettings {
    var motionEnabled by mutableStateOf(true)
    var raiseToEarEnabled by mutableStateOf(true)
    var hapticsEnabled by mutableStateOf(true)
    var accent by mutableStateOf(Accent.UK)
}
