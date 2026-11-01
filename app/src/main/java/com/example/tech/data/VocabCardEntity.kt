package com.example.tech.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One saved flashcard. Table: vocab_cards (see design report, section 4.3). */
@Entity(tableName = "vocab_cards", indices = [Index("topic")])
data class VocabCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val phonetic: String,
    val formality: String,      // Formality.*
    val example1: String,
    val example2: String,
    val antiExample: String,
    val topic: String,          // deck name typed on Screen 1
    val status: String = CardStatus.NEW,
    val createdAt: Long = System.currentTimeMillis(),
)

object CardStatus {
    const val NEW = "NEW"
    const val MASTERED = "MASTERED"
    const val REVIEW = "REVIEW"
}

object Formality {
    const val FORMAL = "FORMAL"
    const val NEUTRAL = "NEUTRAL"
    const val INFORMAL = "INFORMAL"
    const val SLANG = "SLANG"
}
