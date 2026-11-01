package com.example.tech.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VocabCardEntity::class], version = 1, exportSchema = false)
abstract class TechDatabase : RoomDatabase() {

    abstract fun vocabDao(): VocabDao

    companion object {
        @Volatile
        private var instance: TechDatabase? = null

        /** Thread-safe singleton: one database connection for the whole app. */
        fun get(context: Context): TechDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TechDatabase::class.java,
                    "tech.db",
                ).build().also { instance = it }
            }
    }
}
