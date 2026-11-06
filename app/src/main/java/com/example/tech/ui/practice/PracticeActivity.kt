package com.example.tech.ui.practice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tech.ui.theme.TechTheme

/**
 * Screen 2 host. Started by an Explicit Intent carrying:
 *   EXTRA_MODE  = "DECK" | "REVIEW_ALL"
 *   EXTRA_TOPIC = deck name (DECK mode only)
 * The extras reach PracticeViewModel through its SavedStateHandle.
 */
class PracticeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechTheme {
                PracticeScreen(onBack = { finish() })
            }
        }
    }
}
