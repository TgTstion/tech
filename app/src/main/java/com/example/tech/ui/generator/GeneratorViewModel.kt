package com.example.tech.ui.generator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tech.ai.DraftCard
import com.example.tech.data.VocabRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class DraftItem(val card: DraftCard, val selected: Boolean = true)

data class GeneratorUiState(
    val topic: String = "",
    val batchSize: Int = 20,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val items: List<DraftItem> = emptyList(),
) {
    val selectedCount get() = items.count { it.selected }
    val canGenerate get() = topic.isNotBlank() && !loading
}

class GeneratorViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VocabRepository.from(app)

    var state by mutableStateOf(GeneratorUiState())
        private set

    fun onTopicChange(value: String) {
        state = state.copy(topic = value)
    }

    /** Slider gives a Float; snap to exactly 10, 20 or 30. */
    fun onBatchSizeChange(value: Float) {
        state = state.copy(batchSize = ((value / 10f).roundToInt() * 10).coerceIn(10, 30))
    }

    fun generate() {
        if (!state.canGenerate) return
        viewModelScope.launch {
            state = state.copy(loading = true, items = emptyList())
            val drafts = repo.generate(state.topic.trim(), state.batchSize)
            state = state.copy(loading = false, items = drafts.map { DraftItem(it) })
        }
    }

    fun toggle(index: Int) {
        state = state.copy(
            items = state.items.mapIndexed { i, item ->
                if (i == index) item.copy(selected = !item.selected) else item
            }
        )
    }

    fun setAll(selected: Boolean) {
        state = state.copy(items = state.items.map { it.copy(selected = selected) })
    }

    /** CREATE: inserts only the checked cards into Room. */
    fun saveSelected(onSaved: (topic: String, count: Int) -> Unit) {
        val topic = state.topic.trim()
        val chosen = state.items.filter { it.selected }.map { it.card }
        if (topic.isEmpty() || chosen.isEmpty()) return
        viewModelScope.launch {
            state = state.copy(saving = true)
            repo.save(topic, chosen)
            state = state.copy(saving = false, items = emptyList())
            onSaved(topic, chosen.size)
        }
    }
}
