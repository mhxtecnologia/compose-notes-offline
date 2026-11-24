package com.mhxtecnologia.notes.feature.editor.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mhxtecnologia.notes.feature.editor.NoteEditorRoute

internal const val NOTE_ID_ARG = "noteId"

/** Valor da rota para "nota nova" (argumentos Long da navegação não aceitam nulo). */
internal const val NEW_NOTE_ID = -1L

private const val EDITOR_BASE_ROUTE = "editor"
const val EDITOR_ROUTE = "$EDITOR_BASE_ROUTE?$NOTE_ID_ARG={$NOTE_ID_ARG}"

/** Abre o editor de uma nota existente ou, sem [noteId], de uma nota nova. */
fun NavController.navigateToEditor(noteId: Long? = null) {
    navigate("$EDITOR_BASE_ROUTE?$NOTE_ID_ARG=${noteId ?: NEW_NOTE_ID}")
}

fun NavGraphBuilder.editorScreen(onFinished: () -> Unit) {
    composable(
        route = EDITOR_ROUTE,
        arguments = listOf(
            navArgument(NOTE_ID_ARG) {
                type = NavType.LongType
                defaultValue = NEW_NOTE_ID
            },
        ),
    ) {
        NoteEditorRoute(onFinished = onFinished)
    }
}
