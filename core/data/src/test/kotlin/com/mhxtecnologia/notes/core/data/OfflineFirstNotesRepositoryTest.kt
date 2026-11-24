package com.mhxtecnologia.notes.core.data

import app.cash.turbine.test
import com.mhxtecnologia.notes.core.database.NoteEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class OfflineFirstNotesRepositoryTest {

    private val clock = MutableClock(Instant.ofEpochMilli(1_000))
    private val dao = FakeNoteDao()
    private val repository = OfflineFirstNotesRepository(dao, clock)

    @Test
    fun `saving a new note stamps both dates with the current time`() = runTest {
        val id = repository.saveNote(id = null, title = "Groceries", content = "Milk")

        val note = repository.getNote(id)
        assertEquals("Groceries", note?.title)
        assertEquals(Instant.ofEpochMilli(1_000), note?.createdAt)
        assertEquals(Instant.ofEpochMilli(1_000), note?.updatedAt)
    }

    @Test
    fun `saving an existing note keeps createdAt and moves updatedAt`() = runTest {
        val id = repository.saveNote(id = null, title = "Draft", content = "")
        clock.now = Instant.ofEpochMilli(9_000)

        val savedId = repository.saveNote(id = id, title = "Final", content = "Done")

        assertEquals(id, savedId)
        val note = repository.getNote(id)
        assertEquals("Final", note?.title)
        assertEquals(Instant.ofEpochMilli(1_000), note?.createdAt)
        assertEquals(Instant.ofEpochMilli(9_000), note?.updatedAt)
        assertEquals(1, dao.all.size)
    }

    @Test
    fun `saving a note that was deleted meanwhile keeps the text as a new note`() = runTest {
        val savedId = repository.saveNote(id = 99, title = "Lost?", content = "No")

        assertEquals("Lost?", repository.getNote(savedId)?.title)
        assertEquals(1, dao.all.size)
    }

    @Test
    fun `observeNotes emits again on every change, newest first`() = runTest {
        repository.observeNotes().test {
            assertEquals(emptyList<String>(), awaitItem().map { it.title })

            repository.saveNote(id = null, title = "First", content = "")
            assertEquals(listOf("First"), awaitItem().map { it.title })

            clock.now = Instant.ofEpochMilli(2_000)
            repository.saveNote(id = null, title = "Second", content = "")
            assertEquals(listOf("Second", "First"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `observeNotes searches literally, even with LIKE wildcards in the query`() = runTest {
        dao.upsert(entity(title = "Discount", content = "50% off"))
        dao.upsert(entity(title = "Stock", content = "500 items"))

        repository.observeNotes(query = " 50% ").test {
            assertEquals(listOf("Discount"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `a deleted note can be restored exactly as it was`() = runTest {
        val id = repository.saveNote(id = null, title = "Keep me", content = "please")
        val original = repository.getNote(id)!!

        repository.deleteNote(id)
        assertNull(repository.getNote(id))

        repository.restoreNote(original)
        assertEquals(original, repository.getNote(id))
    }

    @Test
    fun `escapeLikePattern escapes wildcards and the escape character`() {
        assertEquals("100\\%", "100%".escapeLikePattern())
        assertEquals("snake\\_case", "snake_case".escapeLikePattern())
        assertEquals("C:\\\\temp", "C:\\temp".escapeLikePattern())
    }

    private fun entity(title: String, content: String) =
        NoteEntity(title = title, content = content, createdAt = 1_000, updatedAt = 1_000)

    /** Relógio que o teste adianta à mão. */
    private class MutableClock(var now: Instant) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
    }
}
