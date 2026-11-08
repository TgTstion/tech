package com.example.tech.ui.hub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tech.data.CardStatus
import com.example.tech.data.VocabCardEntity
import com.example.tech.data.VocabRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HubStats(
    val total: Int = 0,
    val mastered: Int = 0,
    val toReview: Int = 0,
    val wordOfTheDay: VocabCardEntity? = null,
)

class HubViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VocabRepository.from(app)

    // READ via Flow: statistics update live when Screen 2 changes a card's status.
    val stats: StateFlow<HubStats> = repo.observeAll()
        .map { cards ->
            HubStats(
                total = cards.size,
                mastered = cards.count { it.status == CardStatus.MASTERED },
                toReview = cards.count { it.status == CardStatus.REVIEW },
                // Same word all day, changes daily.
                wordOfTheDay = cards.sortedBy { it.id }
                    .getOrNull(LocalDate.now().dayOfYear % cards.size.coerceAtLeast(1)),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubStats())

    val latestTopic: StateFlow<String?> = repo.observeLatestTopic()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
