package com.choretracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Chore::class, ChoreCompletion::class],
    version = 1,
    exportSchema = false
)
abstract class ChoreDatabase : RoomDatabase() {

    abstract fun choreDao(): ChoreDao

    companion object {
        @Volatile
        private var INSTANCE: ChoreDatabase? = null

        fun getInstance(context: Context): ChoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChoreDatabase::class.java,
                    "chore_tracker_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
