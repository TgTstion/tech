package com.example.tech.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.tech.data.VocabCardEntity
import com.example.tech.ui.generator.GeneratorActivity
import com.example.tech.ui.practice.PracticeActivity

/** Every Intent in the app lives here, so the report's Intent table maps 1:1 to code. */
object IntentHelpers {

    const val EXTRA_MODE = "com.example.tech.EXTRA_MODE"
    const val EXTRA_TOPIC = "com.example.tech.EXTRA_TOPIC"
    const val MODE_DECK = "DECK"
    const val MODE_REVIEW_ALL = "REVIEW_ALL"

    // ---------- Explicit Intents (navigation inside the app) ----------

    /** Screen 3 -> Screen 1 */
    fun openGenerator(context: Context) {
        context.startActivity(Intent(context, GeneratorActivity::class.java))
    }

    /** Screen 3 or Screen 1 -> Screen 2, with extras selecting deck or Review All mode. */
    fun openPractice(context: Context, mode: String, topic: String? = null) {
        val intent = Intent(context, PracticeActivity::class.java).apply {
            putExtra(EXTRA_MODE, mode)
            if (topic != null) putExtra(EXTRA_TOPIC, topic)
        }
        context.startActivity(intent)
    }

    // ---------- Implicit Intents (delegate to other apps) ----------

    /** ACTION_SEND: opens the system share sheet (WhatsApp, Gmail, Messages, ...). */
    fun shareText(context: Context, text: String, chooserTitle: String = "Share via") {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "From tech")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        safeStart(context, Intent.createChooser(send, chooserTitle))
    }

    fun shareCard(context: Context, card: VocabCardEntity) {
        val text = buildString {
            appendLine("${card.term}  ${card.phonetic}")
            appendLine("e.g. ${card.example1}")
            append("Learned with tech")
        }
        shareText(context, text, "Share card")
    }

    /** ACTION_VIEW: opens YouGlish in the browser to hear native speakers say the term. */
    fun openYouGlish(context: Context, term: String, accent: Accent = AppSettings.accent) {
        val uri = Uri.parse(
            "https://youglish.com/pronounce/${Uri.encode(term)}/english/${accent.youglishCode}"
        )
        safeStart(context, Intent(Intent.ACTION_VIEW, uri))
    }

    private fun safeStart(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to handle this action", Toast.LENGTH_SHORT).show()
        }
    }
}
