package com.choretracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: Chore)

    @Delete
    suspend fun deleteChore(chore: Chore)

    @Query("SELECT * FROM chores ORDER BY category, name")
    fun getAllChores(): Flow<List<Chore>>

    @Query("SELECT * FROM chores WHERE category = :category ORDER BY name")
    fun getChoresByCategory(category: String): Flow<List<Chore>>

    @Insert
    suspend fun insertCompletion(completion: ChoreCompletion)

    @Query("SELECT * FROM completions WHERE completedDate >= :startMillis AND completedDate <= :endMillis ORDER BY completedDate DESC")
    fun getCompletionsBetween(startMillis: Long, endMillis: Long): Flow<List<ChoreCompletion>>

    @Query("SELECT * FROM completions WHERE choreName = :choreName AND completedDate >= :startMillis AND completedDate <= :endMillis ORDER BY completedDate DESC")
    fun getCompletionsForChoreBetween(choreName: String, startMillis: Long, endMillis: Long): Flow<List<ChoreCompletion>>

    @Query("DELETE FROM completions WHERE completedDate < :cutoffMillis")
    suspend fun deleteCompletionsOlderThan(cutoffMillis: Long)

    @Query("SELECT * FROM completions ORDER BY completedDate DESC")
    fun getAllCompletions(): Flow<List<ChoreCompletion>>

    @Query("DELETE FROM completions WHERE id = :id")
    suspend fun deleteCompletion(id: Long)

    @Query("DELETE FROM completions WHERE choreName = :choreName")
    suspend fun deleteCompletionsForChore(choreName: String)
}
