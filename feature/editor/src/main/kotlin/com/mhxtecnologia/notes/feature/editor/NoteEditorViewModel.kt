package com.mhxtecnologia.notes.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhxtecnologia.notes.core.data.NotesRepository
import com.mhxtecnologia.notes.feature.editor.navigation.NEW_NOTE_ID
import com.mhxtecnologia.notes.feature.editor.navigation.NOTE_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NotesRepository,
) : ViewModel() {

    /** Id vindo da rota; nulo quando o editor abriu para uma nota nova. */
    private val noteId: Long? = savedStateHandle.get<Long>(NOTE_ID_ARG)?.takeIf { it != NEW_NOTE_ID }

    // MutableStateFlow atualizado de forma síncrona: os campos de texto leem
    // daqui sem atraso, então o cursor não pula enquanto o usuário digita.
    private val _uiState = MutableStateFlow(
        NoteEditorUiState(isNewNote = noteId == null, isLoading = noteId != null),
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) load(noteId)
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is NoteEditorEvent.ContentChanged -> _uiState.update { it.copy(content = event.content) }
            NoteEditorEvent.SaveAndClose -> saveAndClose()
        }
    }

    private fun load(id: Long) {
        viewModelScope.launch {
            val note = repository.getNote(id)
            _uiState.update {
                it.copy(
                    title = note?.title.orEmpty(),
                    content = note?.content.orEmpty(),
                    isLoading = false,
                )
            }
        }
    }

    private fun saveAndClose() {
        val state = _uiState.value
        if (state.isSaving || state.isFinished || state.isLoading) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val isBlank = state.title.isBlank() && state.content.isBlank()
            when {
                // Nota nova em branco: não grava nada.
                isBlank && noteId == null -> Unit
                // Nota existente que foi esvaziada: some, em vez de ficar uma nota vazia na lista.
                isBlank && noteId != null -> repository.deleteNote(noteId)
                else -> repository.saveNote(noteId, state.title.trim(), state.content)
            }
            _uiState.update { it.copy(isSaving = false, isFinished = true) }
        }
    }
}
