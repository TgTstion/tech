package com.example.tech.ai

import kotlinx.coroutines.delay

/**
 * Rule-based placeholder for the AI sentence check (Active Sentence Production).
 * Milestone 2 replaces this with an LLM call returning grammar and context feedback.
 */
class MockSentenceValidator : SentenceValidator {

    override suspend fun check(term: String, sentence: String): SentenceFeedback {
        delay(400) // simulate a network round trip
        val s = sentence.trim()
        val words = s.split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            s.isEmpty() ->
                SentenceFeedback(false, "Write a sentence first.")
            !s.contains(term, ignoreCase = true) ->
                SentenceFeedback(false, "Use the target word \"$term\" in your sentence.")
            words.size < 4 ->
                SentenceFeedback(false, "Try a longer sentence (at least four words) so the context is clear.")
            s.first().isLetter() && !s.first().isUpperCase() ->
                SentenceFeedback(false, "Start the sentence with a capital letter.")
            s.last() !in ".!?\"" ->
                SentenceFeedback(false, "Nearly there: finish with punctuation (. ! ?).")
            else ->
                SentenceFeedback(true, "Looks good! (Mock check — AI grammar and context feedback arrives in Milestone 2.)")
        }
    }
}
