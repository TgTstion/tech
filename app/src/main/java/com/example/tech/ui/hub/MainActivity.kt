package com.example.tech.ui.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tech.ui.theme.TechTheme

/** Launcher Activity — hosts Screen 3 (Profile / Settings / Practice Hub). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechTheme {
                HubScreen()
            }
        }
    }
}
