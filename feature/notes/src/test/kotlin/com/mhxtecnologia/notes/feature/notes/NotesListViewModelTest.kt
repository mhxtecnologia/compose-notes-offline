package com.mhxtecnologia.notes.feature.notes

import app.cash.turbine.test
import com.mhxtecnologia.notes.core.model.Note
import com.mhxtecnologia.notes.core.testing.FakeNotesRepository
import com.mhxtecnologia.notes.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository()
    private lateinit var viewModel: NotesListViewModel

    // Criado no @Before (e não na declaração) para nascer depois que a regra troca o Main.
    @Before
    fun createViewModel() {
        viewModel = NotesListViewModel(repository)
    }

    @Test
    fun `starts loading, then lists notes from the most recently edited`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
        repository.setNotes(note(1, "Old", editedAt = 1_000), note(2, "New", editedAt = 2_000))

        collectUiState()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(listOf("New", "Old"), state.notes.map { it.title })
    }

    @Test
    fun `typing a query keeps it in the state and filters the notes`() = runTest {
        repository.setNotes(note(1, "Groceries"), note(2, "Trip ideas"))

        viewModel.uiState.test {
            viewModel.onEvent(NotesListEvent.QueryChanged("groc"))

            val state = expectMostRecentItem()
            assertEquals("groc", state.query)
            assertEquals(listOf("Groceries"), state.notes.map { it.title })
        }
    }

    @Test
    fun `pinning a note moves it to the top, and unpinning puts it back`() = runTest {
        val old = note(1, "Old", editedAt = 1_000)
        repository.setNotes(old, note(2, "New", editedAt = 2_000))
        collectUiState()

        viewModel.onEvent(NotesListEvent.TogglePin(old))
        assertEquals(listOf("Old", "New"), viewModel.uiState.value.notes.map { it.title })

        viewModel.onEvent(NotesListEvent.TogglePin(viewModel.uiState.value.notes.first()))
        assertEquals(listOf("New", "Old"), viewModel.uiState.value.notes.map { it.title })
    }

    @Test
    fun `deleting a note removes it and offers undo, which brings it back`() = runTest {
        val groceries = note(1, "Groceries")
        repository.setNotes(groceries, note(2, "Trip"))
        collectUiState()

        viewModel.onEvent(NotesListEvent.DeleteNote(groceries))

        assertEquals(listOf("Trip"), viewModel.uiState.value.notes.map { it.title })
        assertEquals(groceries, viewModel.uiState.value.recentlyDeleted)

        viewModel.onEvent(NotesListEvent.UndoDelete)

        assertNull(viewModel.uiState.value.recentlyDeleted)
        assertEquals(setOf("Groceries", "Trip"), viewModel.uiState.value.notes.map { it.title }.toSet())
        assertEquals(groceries, repository.getNote(1))
    }

    @Test
    fun `dismissing the undo snackbar keeps the note deleted`() = runTest {
        val groceries = note(1, "Groceries")
        repository.setNotes(groceries)
        collectUiState()

        viewModel.onEvent(NotesListEvent.DeleteNote(groceries))
        viewModel.onEvent(NotesListEvent.UndoDismissed)

        assertNull(viewModel.uiState.value.recentlyDeleted)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertNull(repository.getNote(1))
    }

    /** Assina o StateFlow durante o teste, como a tela faria (o WhileSubscribed só roda com assinante). */
    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    private fun note(id: Long, title: String, editedAt: Long = 1_000) = Note(
        id = id,
        title = title,
        content = "",
        createdAt = Instant.ofEpochMilli(editedAt),
        updatedAt = Instant.ofEpochMilli(editedAt),
    )
}
