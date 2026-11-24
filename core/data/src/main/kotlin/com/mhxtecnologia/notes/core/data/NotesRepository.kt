package com.mhxtecnologia.notes.core.data

import com.mhxtecnologia.notes.core.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Fonte única das notas para as features. Offline-first: o banco local é a
 * verdade, e a UI observa um [Flow] que emite de novo a cada mudança.
 */
interface NotesRepository {

    /** Notas que contêm [query] no título ou no texto; vazio devolve todas. */
    fun observeNotes(query: String = ""): Flow<List<Note>>

    suspend fun getNote(id: Long): Note?

    /**
     * Cria a nota quando [id] é nulo e atualiza quando não é. Se a nota sumiu
     * nesse meio-tempo, o texto não se perde: vira uma nota nova.
     *
     * @return o id da nota gravada.
     */
    suspend fun saveNote(id: Long?, title: String, content: String): Long

    suspend fun deleteNote(id: Long)

    /** Devolve uma nota apagada exatamente como estava (ação de desfazer). */
    suspend fun restoreNote(note: Note)
}
