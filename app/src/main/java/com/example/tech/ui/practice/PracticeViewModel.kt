package com.example.tech.ui.practice

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.tech.ai.SentenceFeedback
import com.example.tech.data.CardStatus
import com.example.tech.data.VocabCardEntity
import com.example.tech.data.VocabRepository
import com.example.tech.util.IntentHelpers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Reads EXTRA_MODE / EXTRA_TOPIC from the Explicit Intent (Activity extras arrive in
 * SavedStateHandle automatically) and exposes the deck plus practice state.
 */
class PracticeViewModel(
    app: Application,
    savedState: SavedStateHandle,
) : AndroidViewModel(app) {

    private val repo = VocabRepository.from(app)

    val topic: String? = savedState.get<String>(IntentHelpers.EXTRA_TOPIC)
    val isReviewAll: Boolean =
        savedState.get<String>(IntentHelpers.EXTRA_MODE) != IntentHelpers.MODE_DECK || topic == null

    /** null = natural order; otherwise the shuffled order of card ids. */
    private val shuffledIds = MutableStateFlow<List<Long>?>(null)

    // READ (Room Flow): recomposes automatically when a card is updated or deleted.
    val cards: StateFlow<List<VocabCardEntity>> = combine(
        if (isReviewAll) repo.observeAll() else repo.observeDeck(topic!!),
        shuffledIds,
    ) { list, order ->
        if (order == null) list
        else {
            val byId = list.associateBy { it.id }
            order.mapNotNull { byId[it] } + list.filter { it.id !in order }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var index by mutableIntStateOf(0)
        private set
    var flipped by mutableStateOf(false)
    var feedback by mutableStateOf<SentenceFeedback?>(null)
        private set
    var checking by mutableStateOf(false)
        private set

    fun currentCard(list: List<VocabCardEntity>): VocabCardEntity? =
        if (list.isEmpty()) null else list[index.coerceIn(0, list.lastIndex)]

    /** Returns true when the round wrapped back to the first card. */
    fun next(size: Int): Boolean {
        flipped = false
        feedback = null
        if (size == 0) return false
        val wrapped = index + 1 >= size
        index = if (wrapped) 0 else index + 1
        return wrapped
    }

    /** UPDATE: tilt right / tilt left / buttons. */
    fun mark(card: VocabCardEntity, mastered: Boolean, size: Int): Boolean {
        viewModelScope.launch {
            repo.setStatus(card.id, if (mastered) CardStatus.MASTERED else CardStatus.REVIEW)
        }
        return next(size)
    }

    fun shuffle(list: List<VocabCardEntity>) {
        shuffledIds.value = list.map { it.id }.shuffled()
        index = 0
        flipped = false
        feedback = null
    }

    /** DELETE: details sheet. */
    fun delete(card: VocabCardEntity, size: Int) {
        viewModelScope.launch { repo.delete(card) }
        if (index >= size - 1) index = 0
        flipped = false
        feedback = null
    }

    /** Active Sentence Production (mock AI validation in Milestone 1). */
    fun checkSentence(term: String, sentence: String) {
        viewModelScope.launch {
            checking = true
            feedback = repo.checkSentence(term, sentence)
            checking = false
        }
    }

    fun clearFeedback() {
        feedback = null
    }
}
