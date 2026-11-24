package com.mhxtecnologia.notes.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhxtecnologia.notes.core.data.NotesRepository
import com.mhxtecnologia.notes.core.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val repository: NotesRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val recentlyDeleted = MutableStateFlow<Note?>(null)

    /**
     * Cada letra digitada troca a consulta; o flatMapLatest cancela a busca
     * anterior e passa a observar só a nova.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val notes = query.flatMapLatest { repository.observeNotes(it) }

    val uiState: StateFlow<NotesListUiState> =
        combine(query, notes, recentlyDeleted) { currentQuery, currentNotes, deleted ->
            NotesListUiState(
                query = currentQuery,
                notes = currentNotes,
                isLoading = false,
                recentlyDeleted = deleted,
            )
        }.stateIn(
            scope = viewModelScope,
            // Mantém a busca viva por 5 s numa rotação de tela, sem refazer a consulta.
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = NotesListUiState(),
        )

    fun onEvent(event: NotesListEvent) {
        when (event) {
            is NotesListEvent.QueryChanged -> query.value = event.query
            is NotesListEvent.TogglePin -> togglePin(event.note)
            is NotesListEvent.DeleteNote -> delete(event.note)
            NotesListEvent.UndoDelete -> undoDelete()
            NotesListEvent.UndoDismissed -> recentlyDeleted.value = null
        }
    }

    private fun togglePin(note: Note) {
        viewModelScope.launch { repository.setPinned(note.id, pinned = !note.isPinned) }
    }

    private fun delete(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note.id)
            recentlyDeleted.value = note
        }
    }

    private fun undoDelete() {
        val note = recentlyDeleted.value ?: return
        recentlyDeleted.value = null
        viewModelScope.launch { repository.restoreNote(note) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
