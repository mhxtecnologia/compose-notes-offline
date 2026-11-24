package com.mhxtecnologia.notes.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Roda as consultas reais do Room num SQLite em memória, no emulador. */
@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: NotesDatabase
    private lateinit var dao: NoteDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotesDatabase::class.java).build()
        dao = database.noteDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun observeNotes_ordersByMostRecentlyUpdated() = runTest {
        dao.upsert(note(title = "Old", updatedAt = 1_000))
        dao.upsert(note(title = "New", updatedAt = 3_000))
        dao.upsert(note(title = "Middle", updatedAt = 2_000))

        val titles = dao.observeNotes(pattern = "").first().map { it.title }

        assertEquals(listOf("New", "Middle", "Old"), titles)
    }

    @Test
    fun observeNotes_matchesTitleOrContentIgnoringCase() = runTest {
        dao.upsert(note(title = "Groceries", content = "milk and eggs"))
        dao.upsert(note(title = "Trip", content = "Buy MILK for the road"))
        dao.upsert(note(title = "Ideas", content = "a notes app"))

        val titles = dao.observeNotes(pattern = "milk").first().map { it.title }.toSet()

        assertEquals(setOf("Groceries", "Trip"), titles)
    }

    @Test
    fun observeNotes_treatsEscapedWildcardsLiterally() = runTest {
        dao.upsert(note(title = "Discount", content = "50% off"))
        dao.upsert(note(title = "Plain", content = "500 items"))

        // O repositório escapa o "%" digitado; aqui conferimos que o ESCAPE da consulta funciona.
        val titles = dao.observeNotes(pattern = "50\\%").first().map { it.title }

        assertEquals(listOf("Discount"), titles)
    }

    @Test
    fun updateContent_changesTextAndUpdatedAtButKeepsCreatedAt() = runTest {
        val id = dao.upsert(note(title = "Draft", createdAt = 1_000, updatedAt = 1_000))

        val changed = dao.updateContent(id, title = "Final", content = "done", updatedAt = 5_000)

        assertEquals(1, changed)
        val stored = dao.getNote(id)
        assertEquals("Final", stored?.title)
        assertEquals(1_000L, stored?.createdAt)
        assertEquals(5_000L, stored?.updatedAt)
    }

    @Test
    fun updateContent_returnsZeroWhenNoteIsGone() = runTest {
        assertEquals(0, dao.updateContent(id = 42, title = "x", content = "y", updatedAt = 1))
    }

    @Test
    fun deleteThenUpsert_restoresTheSameNote() = runTest {
        val original = note(title = "Keep me").copy(id = 7)
        dao.upsert(original)

        dao.delete(7)
        assertNull(dao.getNote(7))

        dao.upsert(original)
        assertEquals(original, dao.getNote(7))
    }

    private fun note(
        title: String,
        content: String = "",
        createdAt: Long = 1_000,
        updatedAt: Long = createdAt,
    ) = NoteEntity(title = title, content = content, createdAt = createdAt, updatedAt = updatedAt)
}
