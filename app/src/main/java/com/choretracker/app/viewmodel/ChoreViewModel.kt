package com.choretracker.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.choretracker.app.data.Chore
import com.choretracker.app.data.ChoreCompletion
import com.choretracker.app.data.ChoreDatabase
import com.choretracker.app.data.ChoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.pow

class ChoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChoreRepository

    val allChores: Flow<List<Chore>>
    val allCompletions: Flow<List<ChoreCompletion>>
    val playerState: Flow<PlayerState>

    init {
        val dao = ChoreDatabase.getInstance(application).choreDao()
        repository = ChoreRepository(dao)
        allChores = repository.allChores
        allCompletions = repository.getAllCompletions()

        playerState = allCompletions.map { completions ->
            val totalXp = completions.sumOf { xpForCategory(it.category) }
            val (level, xpInto) = calculateLevelAndProgress(totalXp)
            PlayerState(level, xpInto, xpToNextLevel(level))
        }
    }

    // --- Chore management ---

    fun addChore(name: String, category: String) {
        viewModelScope.launch {
            repository.addChore(name.trim(), category)
        }
    }

    fun deleteChore(chore: Chore) {
        viewModelScope.launch {
            repository.deleteChore(chore)
        }
    }

    // --- Completion actions ---

    fun completeChore(chore: Chore) {
        viewModelScope.launch {
            repository.completeChore(chore)
        }
    }

    fun undoCompletion(completion: ChoreCompletion) {
        viewModelScope.launch {
            repository.undoCompletion(completion)
        }
    }

    // --- Period helpers ---

    fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTodayEndMillis(): Long {
        return getTodayStartMillis() + 24 * 60 * 60 * 1000 - 1
    }

    fun getWeekStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getWeekEndMillis(): Long {
        return getWeekStartMillis() + 7 * 24 * 60 * 60 * 1000 - 1
    }

    fun getBiweekStartMillis(): Long {
        val weekStart = getWeekStartMillis()
        val weekNumber = getWeekNumber()
        val biweekOffset = weekNumber % 2
        return if (biweekOffset == 0) weekStart
               else weekStart - 7 * 24 * 60 * 60 * 1000
    }

    fun getBiweekEndMillis(): Long {
        return getBiweekStartMillis() + 14 * 24 * 60 * 60 * 1000 - 1
    }

    fun getMonthStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getMonthEndMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun getWeekNumber(): Int {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        return cal.get(Calendar.WEEK_OF_YEAR)
    }

    // --- Completion queries for current periods ---

    fun getTodayCompletions(): Flow<List<ChoreCompletion>> {
        return repository.getCompletionsBetween(getTodayStartMillis(), getTodayEndMillis())
    }

    fun getWeekCompletions(): Flow<List<ChoreCompletion>> {
        return repository.getCompletionsBetween(getWeekStartMillis(), getWeekEndMillis())
    }

    fun getBiweekCompletions(): Flow<List<ChoreCompletion>> {
        return repository.getCompletionsBetween(getBiweekStartMillis(), getBiweekEndMillis())
    }

    fun getMonthCompletions(): Flow<List<ChoreCompletion>> {
        return repository.getCompletionsBetween(getMonthStartMillis(), getMonthEndMillis())
    }

    // --- Pending chores (for Overview) ---

    fun getPendingChores(
        allChores: List<Chore>,
        periodCompletions: List<ChoreCompletion>,
        category: String
    ): List<Chore> {
        val doneNames = periodCompletions
            .filter { it.category == category }
            .map { it.choreName }
            .toSet()
        return allChores
            .filter { it.category == category && it.name !in doneNames }
    }

    // --- Import / Export ---

    suspend fun getExportJson(): String {
        val chores = repository.getAllChoresOnce()
        val json = org.json.JSONObject()
        json.put("app", "ChoreTracker")
        json.put("version", 1)
        val choresArray = org.json.JSONArray()
        for (chore in chores) {
            val choreObj = org.json.JSONObject()
            choreObj.put("name", chore.name)
            choreObj.put("category", chore.category)
            choresArray.put(choreObj)
        }
        json.put("chores", choresArray)
        return json.toString(2)
    }

    fun importChores(jsonString: String, onResult: (Int, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val json = org.json.JSONObject(jsonString)
                val choresArray = json.getJSONArray("chores")
                val existingChores = repository.getAllChoresOnce()
                val existingPairs = existingChores.map { it.name to it.category }.toSet()
                var added = 0
                var skipped = 0
                for (i in 0 until choresArray.length()) {
                    val choreObj = choresArray.getJSONObject(i)
                    val name = choreObj.getString("name").trim()
                    val category = choreObj.getString("category").trim()
                    if (name.isNotBlank() && category.isNotBlank()) {
                        if (name to category in existingPairs) {
                            skipped++
                        } else {
                            repository.addChore(name, category)
                            added++
                        }
                    }
                }
                onResult(added, skipped)
            } catch (_: Exception) {
                onResult(-1, 0)
            }
        }
    }

    companion object {
        const val APP_VERSION = "v0.3"
        const val CATEGORY_DAILY = "daily"
        const val CATEGORY_WEEKLY = "weekly"
        const val CATEGORY_BIWEEKLY = "biweekly"
        const val CATEGORY_MONTHLY = "monthly"

        val CATEGORIES = listOf(CATEGORY_DAILY, CATEGORY_WEEKLY, CATEGORY_BIWEEKLY, CATEGORY_MONTHLY)

        private val CATEGORY_XP = mapOf(
            CATEGORY_DAILY to 10,
            CATEGORY_WEEKLY to 30,
            CATEGORY_BIWEEKLY to 50,
            CATEGORY_MONTHLY to 80
        )
        private const val BASE_LEVEL_XP = 100
        private const val LEVEL_XP_RATIO = 0.1

        fun xpForCategory(category: String): Int = CATEGORY_XP[category] ?: 0

        fun xpToNextLevel(level: Int): Int =
            floor(BASE_LEVEL_XP * (1.0 + LEVEL_XP_RATIO).pow(level - 1)).toInt()

        fun calculateLevelAndProgress(totalXp: Int): Pair<Int, Int> {
            var level = 1
            var remaining = totalXp
            while (true) {
                val needed = xpToNextLevel(level)
                if (remaining < needed) break
                remaining -= needed
                level++
            }
            return level to remaining
        }

        fun categoryDisplayName(category: String): String {
            return when (category) {
                CATEGORY_DAILY -> "Daily"
                CATEGORY_WEEKLY -> "Weekly"
                CATEGORY_BIWEEKLY -> "Biweekly"
                CATEGORY_MONTHLY -> "Monthly"
                else -> category
            }
        }
    }
}

data class PlayerState(val level: Int, val currentXp: Int, val xpToNext: Int)
