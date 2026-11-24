package com.mhxtecnologia.notes.feature.notes.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mhxtecnologia.notes.feature.notes.NotesListRoute

const val NOTES_ROUTE = "notes"

/** Registra a lista de notas no grafo; a navegação para fora sai pelos callbacks. */
fun NavGraphBuilder.notesScreen(
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
) {
    composable(route = NOTES_ROUTE) {
        NotesListRoute(onNoteClick = onNoteClick, onNewNote = onNewNote)
    }
}
