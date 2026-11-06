@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tech.ui.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tech.data.CardStatus
import com.example.tech.data.VocabCardEntity
import com.example.tech.ui.components.ContextBlock
import com.example.tech.ui.components.FormalityChip
import com.example.tech.util.AppSettings
import com.example.tech.util.IntentHelpers
import com.example.tech.util.MotionControls
import com.example.tech.util.RaiseToEar
import com.example.tech.util.rememberSpeaker
import kotlinx.coroutines.launch

/** Screen 2: flashcard practice, motion + proximity controls, details sheet (CRUD). */
@Composable
fun PracticeScreen(
    onBack: () -> Unit,
    vm: PracticeViewModel = viewModel(),
) {
    val cards by vm.cards.collectAsStateWithLifecycle()
    val card = vm.currentCard(cards)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val speaker = rememberSpeaker()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDetails by rememberSaveable { mutableStateOf(false) }

    fun feedback(message: String) {
        if (AppSettings.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    fun mark(mastered: Boolean) {
        val c = card ?: return
        val wrapped = vm.mark(c, mastered, cards.size)
        feedback(
            (if (mastered) "Mastered: ${c.term}" else "Needs review: ${c.term}") +
                if (wrapped) " · round complete" else ""
        )
    }

    fun shuffle() {
        if (cards.size < 2) return
        vm.shuffle(cards)
        feedback("Deck shuffled")
    }

    // ---- Sensors: active only while this screen is visible and the details sheet is closed ----
    MotionControls(
        enabled = AppSettings.motionEnabled && card != null && !showDetails,
        onTiltRight = { mark(mastered = true) },
        onTiltLeft = { mark(mastered = false) },
        onShake = { shuffle() },
    )
    RaiseToEar(enabled = AppSettings.raiseToEarEnabled && card != null) {
        card?.let { speaker.speak(it.term) }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(if (vm.isReviewAll) "Review All" else "Deck: ${vm.topic}")
                            if (cards.isNotEmpty()) {
                                Text(
                                    "${vm.index.coerceAtMost(cards.lastIndex) + 1} / ${cards.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { shuffle() }, enabled = cards.size > 1) {
                            Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle deck")
                        }
                    },
                )
                if (cards.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { (vm.index.coerceAtMost(cards.lastIndex) + 1f) / cards.size },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (card == null) {
            EmptyState(Modifier.padding(padding)) { IntentHelpers.openGenerator(context) }
        } else Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Flashcard(
                card = card,
                flipped = vm.flipped,
                onFlip = { vm.flipped = !vm.flipped },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )

            // Primary actions mirror the gestures: left = review, right = mastered
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { mark(mastered = false) }) {
                    Icon(Icons.Filled.Replay, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Needs review")
                }
                FilledTonalIconButton(
                    onClick = { speaker.speak(card.term) },
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear it")
                }
                Button(onClick = { mark(mastered = true) }) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Mastered")
                }
            }

            // Secondary actions
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { showDetails = true }) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.size(4.dp))
                    Text("Details")
                }
                TextButton(onClick = { IntentHelpers.shareCard(context, card) }) { // ACTION_SEND
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.size(4.dp))
                    Text("Share")
                }
                TextButton(onClick = { IntentHelpers.openYouGlish(context, card.term) }) { // ACTION_VIEW
                    Icon(Icons.Filled.OndemandVideo, contentDescription = null)
                    Spacer(Modifier.size(4.dp))
                    Text("YouGlish")
                }
            }

            Text(
                "Tilt right = mastered · Tilt left = review · Shake = shuffle · Raise to ear = listen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDetails && card != null) {
        DetailsSheet(
            card = card,
            vm = vm,
            onDismiss = {
                showDetails = false
                vm.clearFeedback()
            },
            onDelete = {
                vm.delete(card, cards.size)
                showDetails = false
                feedback("Deleted ${card.term}")
            },
        )
    }
}

/** Tap to flip. Front: term + phonetics + formality. Back: examples + anti-example. */
@Composable
private fun Flashcard(
    card: VocabCardEntity,
    flipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(400),
        label = "flip",
    )
    ElevatedCard(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(onClickLabel = "Flip card") { onFlip() },
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StatusBadge(card.status)
                    Spacer(Modifier.height(12.dp))
                    Text(card.term, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(card.phonetic, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    FormalityChip(card.formality)
                    Spacer(Modifier.height(24.dp))
                    Text("Tap to see it in context", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Counter-rotate so the back side is not mirrored.
                ContextBlock(
                    example1 = card.example1,
                    example2 = card.example2,
                    antiExample = card.antiExample,
                    modifier = Modifier
                        .graphicsLayer { rotationY = 180f }
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val label = when (status) {
        CardStatus.MASTERED -> "Mastered"
        CardStatus.REVIEW -> "Needs review"
        else -> "New"
    }
    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

/** Details + Active Sentence Production + Delete (Room DELETE). */
@Composable
private fun DetailsSheet(
    card: VocabCardEntity,
    vm: PracticeViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    var sentence by rememberSaveable(card.id) { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(card.term, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(card.phonetic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FormalityChip(card.formality)
            }
            Text("Deck: ${card.topic}", style = MaterialTheme.typography.labelMedium)
            ContextBlock(card.example1, card.example2, card.antiExample)

            Text("Your turn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = sentence,
                onValueChange = {
                    sentence = it
                    vm.clearFeedback()
                },
                label = { Text("Write a sentence using \"${card.term}\"") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Button(
                onClick = { vm.checkSentence(card.term, sentence) },
                enabled = !vm.checking && sentence.isNotBlank(),
            ) { Text(if (vm.checking) "Checking…" else "Check") }

            vm.feedback?.let { fb ->
                Text(
                    (if (fb.ok) "✓ " else "✗ ") + fb.message,
                    color = if (fb.ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }

            TextButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(6.dp))
                Text("Delete card", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onGenerate: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No cards yet", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Generate a deck and save some cards to start practising.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGenerate) { Text("Generate a deck") } // Explicit Intent -> Screen 1
    }
}
