package com.example.tech.data

import android.content.Context
import com.example.tech.ai.DeckGenerator
import com.example.tech.ai.DraftCard
import com.example.tech.ai.MockDeckGenerator
import com.example.tech.ai.MockSentenceValidator
import com.example.tech.ai.SentenceFeedback
import com.example.tech.ai.SentenceValidator
import com.example.tech.ai.toEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the UI. Screens never touch Room or the AI service directly,
 * so the mock AI can be swapped for a real Retrofit client without changing any screen.
 */
class VocabRepository(
    private val dao: VocabDao,
    private val generator: DeckGenerator = MockDeckGenerator(),
    private val validator: SentenceValidator = MockSentenceValidator(),
) {
    fun observeAll(): Flow<List<VocabCardEntity>> = dao.observeAll()
    fun observeDeck(topic: String): Flow<List<VocabCardEntity>> = dao.observeByTopic(topic)
    fun observeLatestTopic(): Flow<String?> = dao.observeLatestTopic()

    suspend fun generate(topic: String, count: Int): List<DraftCard> = generator.generate(topic, count)

    suspend fun save(topic: String, drafts: List<DraftCard>) {
        val now = System.currentTimeMillis()
        // +index keeps the generated order when the deck is read back.
        dao.insertAll(drafts.mapIndexed { i, d -> d.toEntity(topic, now + i) })
    }

    suspend fun setStatus(id: Long, status: String) = dao.updateStatus(id, status)
    suspend fun delete(card: VocabCardEntity) = dao.delete(card)
    suspend fun checkSentence(term: String, sentence: String): SentenceFeedback =
        validator.check(term, sentence)

    companion object {
        fun from(context: Context) = VocabRepository(TechDatabase.get(context).vocabDao())
    }
}
