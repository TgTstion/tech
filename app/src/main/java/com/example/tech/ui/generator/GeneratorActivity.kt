package com.example.tech.ui.generator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tech.ui.theme.TechTheme

/** Screen 1 host. Started by an Explicit Intent from the hub (IntentHelpers.openGenerator). */
class GeneratorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechTheme {
                GeneratorScreen(onBack = { finish() })
            }
        }
    }
}
