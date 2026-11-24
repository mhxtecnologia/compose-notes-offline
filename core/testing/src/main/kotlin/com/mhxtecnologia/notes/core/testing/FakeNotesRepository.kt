package com.mhxtecnologia.notes.core.testing

import com.mhxtecnologia.notes.core.data.NotesRepository
import com.mhxtecnologia.notes.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/**
 * Repositório em memória para os testes de ViewModel. Guarda as notas num
 * StateFlow, então quem observa recebe cada mudança, como no Room.
 */
class FakeNotesRepository : NotesRepository {

    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L

    /** Instante usado nas notas gravadas; o teste pode avançá-lo. */
    var now: Instant = Instant.ofEpochMilli(DEFAULT_NOW_MILLIS)

    val all: List<Note> get() = notes.value

    fun setNotes(vararg items: Note) {
        notes.value = items.toList()
        nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun observeNotes(query: String): Flow<List<Note>> = notes.map { list ->
        val needle = query.trim()
        list
            .filter { needle.isEmpty() || it.title.contains(needle, true) || it.content.contains(needle, true) }
            .sortedWith(compareByDescending<Note> { it.updatedAt }.thenByDescending { it.id })
    }

    override suspend fun getNote(id: Long): Note? = notes.value.firstOrNull { it.id == id }

    override suspend fun saveNote(id: Long?, title: String, content: String): Long {
        val existing = id?.let { getNote(it) }
        val saved = existing?.copy(title = title, content = content, updatedAt = now)
            ?: Note(id = nextId++, title = title, content = content, createdAt = now, updatedAt = now)
        notes.update { list -> list.filterNot { it.id == saved.id } + saved }
        return saved.id
    }

    override suspend fun deleteNote(id: Long) {
        notes.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun restoreNote(note: Note) {
        notes.update { list -> list.filterNot { it.id == note.id } + note }
    }

    private companion object {
        const val DEFAULT_NOW_MILLIS = 1_000L
    }
}
