package com.choretracker.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
class ChoreRepositoryTest {

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

    @Test
    fun addChore_appearsInAllChores() = runBlocking {
        repository.addChore("Clean room", "daily")

        val chores = dao.getChoresOnce()
        assertEquals(1, chores.size)
        assertEquals("Clean room", chores[0].name)
        assertEquals("daily", chores[0].category)
    }

    @Test
    fun deleteChore_removesChoreAndCompletions() = runBlocking {
        repository.addChore("Clean room", "daily")
        val chores = dao.getChoresOnce()
        val chore = chores[0]

        repository.completeChore(chore)
        repository.deleteChore(chore)

        val remaining = dao.getChoresOnce()
        assertTrue(remaining.isEmpty())

        val completions = dao.getAllCompletions().first()
        assertTrue(completions.isEmpty())
    }

    @Test
    fun completeChore_createsCompletion() = runBlocking {
        repository.addChore("Walk dog", "weekly")
        val chore = dao.getChoresOnce()[0]

        repository.completeChore(chore)

        val completions = dao.getAllCompletions().first()
        assertEquals(1, completions.size)
        assertEquals("Walk dog", completions[0].choreName)
        assertEquals("weekly", completions[0].category)
    }

    @Test
    fun completeChore_recordsTimestamp() = runBlocking {
        repository.addChore("Test", "daily")
        val chore = dao.getChoresOnce()[0]
        val before = System.currentTimeMillis()

        repository.completeChore(chore)

        val completions = dao.getAllCompletions().first()
        assertTrue(completions[0].completedDate >= before)
        assertTrue(completions[0].completedDate <= System.currentTimeMillis())
    }

    @Test
    fun undoCompletion_removesCompletion() = runBlocking {
        repository.addChore("Read", "daily")
        val chore = dao.getChoresOnce()[0]
        repository.completeChore(chore)
        val completions = dao.getAllCompletions().first()
        val completion = completions[0]

        repository.undoCompletion(completion)

        val remaining = dao.getAllCompletions().first()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun completeChore_thenUndo_thenCompleteAgain() = runBlocking {
        repository.addChore("Meditate", "daily")
        val chore = dao.getChoresOnce()[0]

        repository.completeChore(chore)
        val completions1 = dao.getAllCompletions().first()
        assertEquals(1, completions1.size)

        repository.undoCompletion(completions1[0])
        assertTrue(dao.getAllCompletions().first().isEmpty())

        repository.completeChore(chore)
        assertEquals(1, dao.getAllCompletions().first().size)
    }

    @Test
    fun getCompletionsBetween_respectsBoundaries() = runBlocking {
        repository.addChore("Task", "daily")
        val chore = dao.getChoresOnce()[0]
        val now = System.currentTimeMillis()

        repository.completeChore(chore, now - 5000)
        repository.completeChore(chore, now + 5000)

        val inRange = dao.getCompletionsBetween(now - 1000, now + 1000).first()
        assertEquals(0, inRange.size)

        val wideRange = dao.getCompletionsBetween(now - 10000, now + 10000).first()
        assertEquals(2, wideRange.size)
    }

    @Test
    fun deleteChore_doesNotAffectOtherChores() = runBlocking {
        repository.addChore("Chore A", "daily")
        repository.addChore("Chore B", "weekly")
        val chores = dao.getChoresOnce()
        val choreA = chores.find { it.name == "Chore A" }!!

        repository.deleteChore(choreA)

        val remaining = dao.getChoresOnce()
        assertEquals(1, remaining.size)
        assertEquals("Chore B", remaining[0].name)
    }
}
