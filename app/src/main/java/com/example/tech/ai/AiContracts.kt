package com.example.tech.ai

import com.example.tech.data.VocabCardEntity

/** A card produced by the generator but not yet saved (Screen 1 batch list). */
data class DraftCard(
    val term: String,
    val phonetic: String,
    val formality: String,
    val example1: String,
    val example2: String,
    val antiExample: String,
)

fun DraftCard.toEntity(topic: String, createdAt: Long) = VocabCardEntity(
    term = term,
    phonetic = phonetic,
    formality = formality,
    example1 = example1,
    example2 = example2,
    antiExample = antiExample,
    topic = topic,
    createdAt = createdAt,
)

/** Milestone 1: MockDeckGenerator. Milestone 2: Retrofit client calling an LLM API. */
interface DeckGenerator {
    suspend fun generate(topic: String, count: Int): List<DraftCard>
}

data class SentenceFeedback(val ok: Boolean, val message: String)

/** Milestone 1: MockSentenceValidator. Milestone 2: LLM grammar + context check. */
interface SentenceValidator {
    suspend fun check(term: String, sentence: String): SentenceFeedback
}
