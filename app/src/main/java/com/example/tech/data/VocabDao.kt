package com.example.tech.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** CRUD mapping — see the table in section 4.3 of the design report. */
@Dao
interface VocabDao {

    // CREATE — "Save selected" on Screen 1
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<VocabCardEntity>)

    // READ — Review All mode (Screen 2) and hub statistics (Screen 3)
    @Query("SELECT * FROM vocab_cards ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<VocabCardEntity>>

    // READ — a single deck (Screen 2)
    @Query("SELECT * FROM vocab_cards WHERE topic = :topic ORDER BY createdAt ASC, id ASC")
    fun observeByTopic(topic: String): Flow<List<VocabCardEntity>>

    // READ — most recently saved deck, for the hub's "Practise latest deck" tile
    @Query("SELECT topic FROM vocab_cards ORDER BY createdAt DESC, id DESC LIMIT 1")
    fun observeLatestTopic(): Flow<String?>

    // UPDATE — tilt gestures / Mastered and Needs review buttons (Screen 2)
    @Query("UPDATE vocab_cards SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    // DELETE — "Delete card" in the details sheet (Screen 2)
    @Delete
    suspend fun delete(card: VocabCardEntity)

    // DELETE — discard a whole deck (wired to the UI in Milestone 2)
    @Query("DELETE FROM vocab_cards WHERE topic = :topic")
    suspend fun deleteTopic(topic: String)
}
