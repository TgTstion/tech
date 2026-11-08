@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tech.ui.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tech.ui.components.FormalityChip
import com.example.tech.util.Accent
import com.example.tech.util.AppSettings
import com.example.tech.util.IntentHelpers

/** Screen 3: profile + stats, word of the day, practice hub (Explicit Intents), settings, sharing. */
@Composable
fun HubScreen(vm: HubViewModel = viewModel()) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    val latestTopic by vm.latestTopic.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("tech", fontWeight = FontWeight.Bold) }) },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Profile header + statistics (Room READ via Flow)
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("L", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Learner", style = MaterialTheme.typography.titleLarge)
                        Text("English · context-first practice", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Total words", stats.total, Modifier.weight(1f))
                    StatTile("Mastered", stats.mastered, Modifier.weight(1f))
                    StatTile("To review", stats.toReview, Modifier.weight(1f))
                }
            }

            // 2. Word of the day: Implicit Intents ACTION_SEND + ACTION_VIEW
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Word of the day", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        val word = stats.wordOfTheDay
                        if (word == null) {
                            Text("Generate your first deck to see a word of the day.")
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(word.term, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text(word.phonetic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                FormalityChip(word.formality)
                            }
                            Text(word.example1, style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { IntentHelpers.shareCard(context, word) }) {
                                    Icon(Icons.Filled.Share, contentDescription = null)
                                    Spacer(Modifier.size(4.dp))
                                    Text("Share")
                                }
                                TextButton(onClick = { IntentHelpers.openYouGlish(context, word.term) }) {
                                    Icon(Icons.Filled.OndemandVideo, contentDescription = null)
                                    Spacer(Modifier.size(4.dp))
                                    Text("Hear on YouGlish")
                                }
                            }
                        }
                    }
                }
            }

            // 3. Practice hub: Explicit Intents to Screen 1 and Screen 2
            item { SectionTitle("Practice hub") }
            item {
                HubTile(Icons.Filled.AutoAwesome, "Generate new deck", "Pick a topic, choose 10, 20 or 30 words") {
                    IntentHelpers.openGenerator(context)
                }
            }
            item {
                val topic = latestTopic
                HubTile(
                    Icons.Filled.Style,
                    "Practise latest deck",
                    topic?.let { "Continue \"$it\"" } ?: "No deck saved yet",
                    enabled = topic != null,
                ) {
                    if (topic != null) IntentHelpers.openPractice(context, IntentHelpers.MODE_DECK, topic)
                }
            }
            item {
                HubTile(Icons.Filled.School, "Review all", "Every saved word, ${stats.total} in total") {
                    IntentHelpers.openPractice(context, IntentHelpers.MODE_REVIEW_ALL)
                }
            }

            // 4. Settings (in-memory for Milestone 1)
            item { SectionTitle("Settings") }
            item {
                Card(Modifier.fillMaxWidth()) {
                    SettingSwitch("Motion controls", "Tilt to grade, shake to shuffle", AppSettings.motionEnabled) {
                        AppSettings.motionEnabled = it
                    }
                    SettingSwitch("Raise-to-ear audio", "Proximity sensor plays pronunciation", AppSettings.raiseToEarEnabled) {
                        AppSettings.raiseToEarEnabled = it
                    }
                    SettingSwitch("Haptic feedback", "Vibrate when a gesture is recognised", AppSettings.hapticsEnabled) {
                        AppSettings.hapticsEnabled = it
                    }
                    Column(Modifier.padding(16.dp)) {
                        Text("Target accent", style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Accent.entries.forEach { accent ->
                                FilterChip(
                                    selected = AppSettings.accent == accent,
                                    onClick = { AppSettings.accent = accent },
                                    label = { Text(accent.label) },
                                )
                            }
                        }
                    }
                }
            }

            // 5. Share progress: Implicit Intent ACTION_SEND
            item {
                FilledTonalButton(
                    onClick = {
                        IntentHelpers.shareText(
                            context,
                            "I've mastered ${stats.mastered} of ${stats.total} words on tech! " +
                                "Learning vocabulary in context, one deck at a time.",
                            "Share my progress",
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Share my progress")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun StatTile(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text("$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HubTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) },
    )
}
