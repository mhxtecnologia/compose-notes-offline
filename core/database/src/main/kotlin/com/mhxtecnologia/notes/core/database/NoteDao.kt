package com.mhxtecnologia.notes.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /**
     * Notas cujo título ou texto contém [pattern]: fixadas primeiro, depois da editada
     * mais recente para a mais antiga.
     * O [pattern] já chega escapado (`\%`, `\_`, `\\`), para que o que o usuário digita
     * seja procurado literalmente e não vire curinga do LIKE.
     */
    @Query(
        """
        SELECT * FROM notes
        WHERE :pattern = ''
            OR title LIKE '%' || :pattern || '%' ESCAPE '\'
            OR content LIKE '%' || :pattern || '%' ESCAPE '\'
        ORDER BY is_pinned DESC, updated_at DESC, id DESC
        """,
    )
    fun observeNotes(pattern: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): NoteEntity?

    /** Insere uma nota nova ou, com o mesmo id, devolve uma apagada (desfazer). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    /** Devolve quantas linhas mudaram: 0 quando a nota já não existe. */
    @Query("UPDATE notes SET title = :title, content = :content, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateContent(id: Long, title: String, content: String, updatedAt: Long): Int

    /** Fixar não conta como edição: o updated_at fica como está. */
    @Query("UPDATE notes SET is_pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long): Int
}
