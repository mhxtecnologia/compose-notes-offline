package com.mhxtecnologia.notes.feature.notes

import com.mhxtecnologia.notes.core.model.Note

/** Tudo o que a tela da lista precisa para se desenhar. Imutável. */
data class NotesListUiState(
    val query: String = "",
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    /** Nota recém-apagada, enquanto o snackbar de "desfazer" está na tela. */
    val recentlyDeleted: Note? = null,
)

/** Tudo o que a tela da lista pode pedir ao ViewModel (fluxo unidirecional). */
sealed interface NotesListEvent {
    data class QueryChanged(val query: String) : NotesListEvent
    data class TogglePin(val note: Note) : NotesListEvent
    data class DeleteNote(val note: Note) : NotesListEvent
    data object UndoDelete : NotesListEvent
    data object UndoDismissed : NotesListEvent
}
