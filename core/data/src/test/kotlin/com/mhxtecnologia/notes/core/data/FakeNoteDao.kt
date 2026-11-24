package com.mhxtecnologia.notes.core.data

import com.mhxtecnologia.notes.core.database.NoteDao
import com.mhxtecnologia.notes.core.database.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * DAO em memória que imita as regras do SQL real (ordem, busca e escape do LIKE).
 * Um fake em vez de mock: o teste verifica comportamento, não chamadas.
 */
class FakeNoteDao : NoteDao {

    private val rows = MutableStateFlow<List<NoteEntity>>(emptyList())
    private var nextId = 1L

    val all: List<NoteEntity> get() = rows.value

    override fun observeNotes(pattern: String): Flow<List<NoteEntity>> = rows.map { entities ->
        val needle = pattern.unescapeLikePattern()
        entities
            .filter { needle.isEmpty() || it.title.contains(needle, true) || it.content.contains(needle, true) }
            .sortedWith(compareByDescending<NoteEntity> { it.updatedAt }.thenByDescending { it.id })
    }

    override suspend fun getNote(id: Long): NoteEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun upsert(note: NoteEntity): Long {
        val id = if (note.id == 0L) nextId++ else note.id
        rows.update { current -> current.filterNot { it.id == id } + note.copy(id = id) }
        return id
    }

    override suspend fun updateContent(id: Long, title: String, content: String, updatedAt: Long): Int {
        if (rows.value.none { it.id == id }) return 0
        rows.update { current ->
            current.map { if (it.id == id) it.copy(title = title, content = content, updatedAt = updatedAt) else it }
        }
        return 1
    }

    override suspend fun delete(id: Long): Int {
        val before = rows.value.size
        rows.update { current -> current.filterNot { it.id == id } }
        return before - rows.value.size
    }

    private fun String.unescapeLikePattern(): String =
        replace("\\%", "%").replace("\\_", "_").replace("\\\\", "\\")
}
