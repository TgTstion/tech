package com.example.tech.util

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Thin wrapper around Android's built-in TextToSpeech engine. */
class Speaker(context: Context) {

    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
    }

    fun speak(text: String) {
        if (!ready) return
        tts.language = AppSettings.accent.locale
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tech-term")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

/** One Speaker per screen, released when the screen leaves composition. */
@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    DisposableEffect(Unit) { onDispose { speaker.shutdown() } }
    return speaker
}
