package com.mhxtecnologia.notes.feature.editor

/** Estado imutável do editor. [isFinished] avisa a tela que pode voltar. */
data class NoteEditorUiState(
    val title: String = "",
    val content: String = "",
    val isNewNote: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
)

sealed interface NoteEditorEvent {
    data class TitleChanged(val title: String) : NoteEditorEvent
    data class ContentChanged(val content: String) : NoteEditorEvent

    /** Voltar salva: como num caderno, não existe "sair sem salvar". */
    data object SaveAndClose : NoteEditorEvent
}
