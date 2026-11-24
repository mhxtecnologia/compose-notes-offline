package com.mhxtecnologia.notes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mhxtecnologia.notes.feature.editor.navigation.EDITOR_ROUTE
import com.mhxtecnologia.notes.feature.editor.navigation.editorScreen
import com.mhxtecnologia.notes.feature.editor.navigation.navigateToEditor
import com.mhxtecnologia.notes.feature.notes.navigation.NOTES_ROUTE
import com.mhxtecnologia.notes.feature.notes.navigation.notesScreen

/**
 * Único lugar que conhece as duas features: liga a lista ao editor.
 * As features não dependem uma da outra.
 */
@Composable
fun NotesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = NOTES_ROUTE,
        modifier = modifier,
    ) {
        notesScreen(
            onNoteClick = { noteId -> navController.navigateToEditor(noteId) },
            onNewNote = { navController.navigateToEditor() },
        )
        editorScreen(
            // Tira só o editor da pilha: se o aviso chegar duas vezes, a segunda não faz nada.
            onFinished = { navController.popBackStack(route = EDITOR_ROUTE, inclusive = true) },
        )
    }
}
