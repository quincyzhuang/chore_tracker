package com.choretracker.app.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.choretracker.app.data.Chore
import com.choretracker.app.data.ChoreDao
import com.choretracker.app.data.ChoreDatabase
import com.choretracker.app.data.ChoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChoreViewModelTest {

    private lateinit var db: ChoreDatabase
    private lateinit var dao: ChoreDao
    private lateinit var repository: ChoreRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ChoreDatabase::class.java).build()
        dao = db.choreDao()
        repository = ChoreRepository(dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- Export (suspend function, directly testable) ---

    @Test
    fun exportJson_containsAllChores() = runBlocking {
        val vm = ChoreViewModel(
            ApplicationProvider.getApplicationContext(),
            repository
        )
        dao.insertChore(Chore(name = "Clean room", category = "daily"))
        dao.insertChore(Chore(name = "Walk dog", category = "weekly"))

        val json = vm.getExportJson()

        assertTrue(json.contains("\"Clean room\""))
        assertTrue(json.contains("\"daily\""))
        assertTrue(json.contains("\"Walk dog\""))
        assertTrue(json.contains("\"weekly\""))
        assertTrue(json.contains("\"ChoreTracker\""))
    }

    @Test
    fun exportJson_emptyChores() = runBlocking {
        val vm = ChoreViewModel(
            ApplicationProvider.getApplicationContext(),
            repository
        )

        val json = vm.getExportJson()

        assertTrue(json.contains("\"chores\""))
        assertFalse(json.contains("name"))
    }

    @Test
    fun exportJson_validJsonFormat() = runBlocking {
        val vm = ChoreViewModel(
            ApplicationProvider.getApplicationContext(),
            repository
        )
        dao.insertChore(Chore(name = "Test", category = "daily"))

        val json = vm.getExportJson()

        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"app\": \"ChoreTracker\""))
        assertTrue(json.contains("\"version\": 1"))
    }

    // --- Import logic (tested via repository, same algorithm as ViewModel) ---

    @Test
    fun import_addsNewChores() = runBlocking {
        val json = """{"app":"ChoreTracker","version":1,"chores":[{"name":"New chore","category":"daily"}]}"""
        val root = org.json.JSONObject(json)
        val choresArray = root.getJSONArray("chores")
        val existing = repository.getAllChoresOnce()
        val existingPairs = existing.map { it.name to it.category }.toSet()
        var added = 0
        var skipped = 0

        for (i in 0 until choresArray.length()) {
            val obj = choresArray.getJSONObject(i)
            val name = obj.getString("name").trim()
            val category = obj.getString("category").trim()
            if (name.isNotBlank() && category.isNotBlank()) {
                if (name to category in existingPairs) {
                    skipped++
                } else {
                    repository.addChore(name, category)
                    added++
                }
            }
        }

        assertEquals(1, added)
        assertEquals(0, skipped)
        val chores = dao.getChoresOnce()
        assertEquals(1, chores.size)
        assertEquals("New chore", chores[0].name)
    }

    @Test
    fun import_skipsDuplicates() = runBlocking {
        repository.addChore("Existing", "daily")

        val json = """{"app":"ChoreTracker","version":1,"chores":[{"name":"Existing","category":"daily"},{"name":"New","category":"weekly"}]}"""
        val root = org.json.JSONObject(json)
        val choresArray = root.getJSONArray("chores")
        val existing = repository.getAllChoresOnce()
        val existingPairs = existing.map { it.name to it.category }.toSet()
        var added = 0
        var skipped = 0

        for (i in 0 until choresArray.length()) {
            val obj = choresArray.getJSONObject(i)
            val name = obj.getString("name").trim()
            val category = obj.getString("category").trim()
            if (name.isNotBlank() && category.isNotBlank()) {
                if (name to category in existingPairs) {
                    skipped++
                } else {
                    repository.addChore(name, category)
                    added++
                }
            }
        }

        assertEquals(1, added)
        assertEquals(1, skipped)
        assertEquals(2, dao.getChoresOnce().size)
    }

    @Test
    fun import_skipsSameNameDifferentCategory() = runBlocking {
        repository.addChore("Task", "daily")

        val json = """{"app":"ChoreTracker","version":1,"chores":[{"name":"Task","category":"weekly"}]}"""
        val root = org.json.JSONObject(json)
        val choresArray = root.getJSONArray("chores")
        val existing = repository.getAllChoresOnce()
        val existingPairs = existing.map { it.name to it.category }.toSet()
        var added = 0
        var skipped = 0

        for (i in 0 until choresArray.length()) {
            val obj = choresArray.getJSONObject(i)
            val name = obj.getString("name").trim()
            val category = obj.getString("category").trim()
            if (name.isNotBlank() && category.isNotBlank()) {
                if (name to category in existingPairs) {
                    skipped++
                } else {
                    repository.addChore(name, category)
                    added++
                }
            }
        }

        assertEquals(1, added)
        assertEquals(0, skipped)
        assertEquals(2, dao.getChoresOnce().size)
    }

    @Test
    fun import_additiveMerge() = runBlocking {
        repository.addChore("Keep", "daily")

        val json = """{"app":"ChoreTracker","version":1,"chores":[{"name":"Keep","category":"daily"},{"name":"Also keep","category":"weekly"},{"name":"Keep","category":"monthly"}]}"""
        val root = org.json.JSONObject(json)
        val choresArray = root.getJSONArray("chores")
        val existing = repository.getAllChoresOnce()
        val existingPairs = existing.map { it.name to it.category }.toSet()
        var added = 0
        var skipped = 0

        for (i in 0 until choresArray.length()) {
            val obj = choresArray.getJSONObject(i)
            val name = obj.getString("name").trim()
            val category = obj.getString("category").trim()
            if (name.isNotBlank() && category.isNotBlank()) {
                if (name to category in existingPairs) {
                    skipped++
                } else {
                    repository.addChore(name, category)
                    added++
                }
            }
        }

        assertEquals(2, added)
        assertEquals(1, skipped)
        assertEquals(3, dao.getChoresOnce().size)
    }

    @Test
    fun import_trimsWhitespace() = runBlocking {
        val json = """{"app":"ChoreTracker","version":1,"chores":[{"name":"  Spaced  ","category":"  daily  "}]}"""
        val root = org.json.JSONObject(json)
        val choresArray = root.getJSONArray("chores")
        val existing = repository.getAllChoresOnce()
        val existingPairs = existing.map { it.name to it.category }.toSet()

        for (i in 0 until choresArray.length()) {
            val obj = choresArray.getJSONObject(i)
            val name = obj.getString("name").trim()
            val category = obj.getString("category").trim()
            if (name.isNotBlank() && category.isNotBlank()) {
                if (name to category !in existingPairs) {
                    repository.addChore(name, category)
                }
            }
        }

        val chores = dao.getChoresOnce()
        assertEquals(1, chores.size)
        assertEquals("Spaced", chores[0].name)
        assertEquals("daily", chores[0].category)
    }
}
