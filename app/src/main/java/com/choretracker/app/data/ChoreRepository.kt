package com.choretracker.app.data

import kotlinx.coroutines.flow.Flow

class ChoreRepository(private val choreDao: ChoreDao) {

    val allChores: Flow<List<Chore>> = choreDao.getAllChores()

    fun getChoresByCategory(category: String): Flow<List<Chore>> =
        choreDao.getChoresByCategory(category)

    suspend fun addChore(name: String, category: String) {
        choreDao.insertChore(Chore(name = name, category = category))
    }

    suspend fun deleteChore(chore: Chore) {
        choreDao.deleteCompletionsForChore(chore.name)
        choreDao.deleteChore(chore)
    }

    suspend fun completeChore(chore: Chore, timestamp: Long = System.currentTimeMillis()) {
        choreDao.insertCompletion(
            ChoreCompletion(
                choreName = chore.name,
                category = chore.category,
                completedDate = timestamp
            )
        )
    }

    suspend fun undoCompletion(completion: ChoreCompletion) {
        choreDao.deleteCompletion(completion.id)
    }

    fun getCompletionsBetween(startMillis: Long, endMillis: Long): Flow<List<ChoreCompletion>> =
        choreDao.getCompletionsBetween(startMillis, endMillis)

    fun getCompletionsForChoreBetween(
        choreName: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ChoreCompletion>> =
        choreDao.getCompletionsForChoreBetween(choreName, startMillis, endMillis)

    fun getAllCompletions(): Flow<List<ChoreCompletion>> =
        choreDao.getAllCompletions()

    suspend fun cleanupOldHistory() {
        val cutoff = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
        choreDao.deleteCompletionsOlderThan(cutoff)
    }
}
