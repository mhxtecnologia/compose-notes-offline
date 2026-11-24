package com.mhxtecnologia.notes.feature.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mhxtecnologia.notes.core.model.Note
import com.mhxtecnologia.notes.core.testing.FakeNotesRepository
import com.mhxtecnologia.notes.core.testing.MainDispatcherRule
import com.mhxtecnologia.notes.feature.editor.navigation.NEW_NOTE_ID
import com.mhxtecnologia.notes.feature.editor.navigation.NOTE_ID_ARG
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class NoteEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository()

    private val groceries = Note(
        id = 7,
        title = "Groceries",
        content = "Milk",
        createdAt = Instant.ofEpochMilli(1_000),
        updatedAt = Instant.ofEpochMilli(1_000),
    )

    @Test
    fun `opening an existing note loads its text`() = runTest {
        repository.setNotes(groceries)

        val state = editorFor(noteId = 7).uiState.value

        assertFalse(state.isNewNote)
        assertFalse(state.isLoading)
        assertEquals("Groceries", state.title)
        assertEquals("Milk", state.content)
    }

    @Test
    fun `typing updates the state synchronously`() = runTest {
        val viewModel = editorFor(noteId = NEW_NOTE_ID)

        viewModel.uiState.test {
            assertEquals("", awaitItem().title)

            viewModel.onEvent(NoteEditorEvent.TitleChanged("Ideas"))
            assertEquals("Ideas", awaitItem().title)

            viewModel.onEvent(NoteEditorEvent.ContentChanged("A notes app"))
            assertEquals("A notes app", awaitItem().content)
        }
    }

    @Test
    fun `saving a new note creates it with a trimmed title and finishes`() = runTest {
        val viewModel = editorFor(noteId = NEW_NOTE_ID)
        viewModel.onEvent(NoteEditorEvent.TitleChanged("  Ideas  "))
        viewModel.onEvent(NoteEditorEvent.ContentChanged("A notes app"))

        viewModel.onEvent(NoteEditorEvent.SaveAndClose)

        assertTrue(viewModel.uiState.value.isFinished)
        val saved = repository.all.single()
        assertEquals("Ideas", saved.title)
        assertEquals("A notes app", saved.content)
    }

    @Test
    fun `saving an existing note updates it instead of creating another`() = runTest {
        repository.setNotes(groceries)
        repository.now = Instant.ofEpochMilli(5_000)
        val viewModel = editorFor(noteId = 7)

        viewModel.onEvent(NoteEditorEvent.ContentChanged("Milk and eggs"))
        viewModel.onEvent(NoteEditorEvent.SaveAndClose)

        val saved = repository.all.single()
        assertEquals(7L, saved.id)
        assertEquals("Milk and eggs", saved.content)
        assertEquals(Instant.ofEpochMilli(1_000), saved.createdAt)
        assertEquals(Instant.ofEpochMilli(5_000), saved.updatedAt)
    }

    @Test
    fun `closing a blank new note saves nothing`() = runTest {
        val viewModel = editorFor(noteId = NEW_NOTE_ID)

        viewModel.onEvent(NoteEditorEvent.TitleChanged("   "))
        viewModel.onEvent(NoteEditorEvent.SaveAndClose)

        assertTrue(viewModel.uiState.value.isFinished)
        assertTrue(repository.all.isEmpty())
    }

    @Test
    fun `emptying an existing note deletes it`() = runTest {
        repository.setNotes(groceries)
        val viewModel = editorFor(noteId = 7)

        viewModel.onEvent(NoteEditorEvent.TitleChanged(""))
        viewModel.onEvent(NoteEditorEvent.ContentChanged(""))
        viewModel.onEvent(NoteEditorEvent.SaveAndClose)

        assertTrue(viewModel.uiState.value.isFinished)
        assertNull(repository.getNote(7))
    }

    @Test
    fun `saving twice in a row only writes once`() = runTest {
        val viewModel = editorFor(noteId = NEW_NOTE_ID)
        viewModel.onEvent(NoteEditorEvent.TitleChanged("Once"))

        viewModel.onEvent(NoteEditorEvent.SaveAndClose)
        viewModel.onEvent(NoteEditorEvent.SaveAndClose)

        assertEquals(1, repository.all.size)
    }

    private fun editorFor(noteId: Long) = NoteEditorViewModel(
        savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to noteId)),
        repository = repository,
    )
}
