@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tech.ui.generator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tech.ui.components.FormalityChip
import com.example.tech.util.IntentHelpers
import kotlinx.coroutines.launch

private val SUGGESTIONS = listOf("IT / Coding", "Dublin slang", "Gaming")

/** Screen 1: topic input, 10/20/30 slider, generated batch with checkboxes, save to Room. */
@Composable
fun GeneratorScreen(
    onBack: () -> Unit,
    vm: GeneratorViewModel = viewModel(),
) {
    val state = vm.state
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate a deck") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = {
                            vm.saveSelected { topic, count ->
                                scope.launch {
                                    val result = snackbar.showSnackbar(
                                        message = "Saved $count cards to \"$topic\"",
                                        actionLabel = "Practise now",
                                        duration = SnackbarDuration.Long,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        // Explicit Intent with extras -> Screen 2 (deck mode)
                                        IntentHelpers.openPractice(context, IntentHelpers.MODE_DECK, topic)
                                    }
                                }
                            }
                        },
                        enabled = state.selectedCount > 0 && !state.saving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(52.dp),
                    ) {
                        Text("Save ${state.selectedCount} cards")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
        ) {
            // 1. Topic input
            OutlinedTextField(
                value = state.topic,
                onValueChange = vm::onTopicChange,
                label = { Text("Topic") },
                placeholder = { Text("e.g. DevOps terms, Dublin pub slang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SUGGESTIONS.forEach { s ->
                    SuggestionChip(onClick = { vm.onTopicChange(s) }, label = { Text(s) })
                }
            }

            // 2. Batch-size slider (snaps to 10 / 20 / 30)
            Spacer(Modifier.height(8.dp))
            Text(
                "Batch size: ${state.batchSize} words",
                style = MaterialTheme.typography.titleSmall,
            )
            Slider(
                value = state.batchSize.toFloat(),
                onValueChange = vm::onBatchSizeChange,
                valueRange = 10f..30f,
                steps = 1, // one stop between the ends -> 10, 20, 30
            )

            // 3. Generate
            Button(
                onClick = vm::generate,
                enabled = state.canGenerate,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Generate")
                }
            }

            // 4. Batch Selection list
            if (state.items.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${state.selectedCount} of ${state.items.size} selected",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.setAll(true) }) { Text("Select all") }
                    TextButton(onClick = { vm.setAll(false) }) { Text("Clear") }
                }
                LazyColumn(Modifier.weight(1f)) {
                    itemsIndexed(state.items) { index, item ->
                        ListItem(
                            modifier = Modifier.clickable { vm.toggle(index) },
                            leadingContent = {
                                Checkbox(checked = item.selected, onCheckedChange = { vm.toggle(index) })
                            },
                            headlineContent = {
                                Text(item.card.term, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Text(item.card.example1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            trailingContent = { FormalityChip(item.card.formality) },
                        )
                        HorizontalDivider()
                    }
                }
            } else if (!state.loading) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Pick a topic and batch size, then tap Generate.\n" +
                            "You can untick any card before saving.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
