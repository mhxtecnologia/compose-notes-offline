package com.mhxtecnologia.notes.core.data

import com.mhxtecnologia.notes.core.database.NoteDao
import com.mhxtecnologia.notes.core.database.NoteEntity
import com.mhxtecnologia.notes.core.database.asEntity
import com.mhxtecnologia.notes.core.database.asExternalModel
import com.mhxtecnologia.notes.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class OfflineFirstNotesRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val clock: Clock,
) : NotesRepository {

    override fun observeNotes(query: String): Flow<List<Note>> =
        noteDao.observeNotes(query.trim().escapeLikePattern())
            .map { entities -> entities.map(NoteEntity::asExternalModel) }

    override suspend fun getNote(id: Long): Note? = noteDao.getNote(id)?.asExternalModel()

    override suspend fun saveNote(id: Long?, title: String, content: String): Long {
        val now = clock.millis()
        if (id != null && noteDao.updateContent(id, title, content, now) > 0) return id
        return noteDao.upsert(NoteEntity(title = title, content = content, createdAt = now, updatedAt = now))
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.delete(id)
    }

    override suspend fun restoreNote(note: Note) {
        noteDao.upsert(note.asEntity())
    }
}

/**
 * Escapa os curingas do LIKE (`%` e `_`) e o próprio caractere de escape, para
 * que a busca por "50%" encontre "50%" e não "500". Par do `ESCAPE '\'` do DAO.
 */
internal fun String.escapeLikePattern(): String =
    replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
