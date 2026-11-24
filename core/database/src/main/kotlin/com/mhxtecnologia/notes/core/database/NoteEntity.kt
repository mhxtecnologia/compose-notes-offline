package com.mhxtecnologia.notes.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mhxtecnologia.notes.core.model.Note
import java.time.Instant

/**
 * Linha da tabela `notes`. As datas ficam em milissegundos desde a época (UTC).
 * O índice cobre a ordem da lista (fixadas primeiro, depois a edição mais recente).
 */
@Entity(
    tableName = "notes",
    indices = [Index(value = ["is_pinned", "updated_at"])],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    /** Coluna da versão 2; o DEFAULT 0 é o mesmo que a migration usa nas linhas antigas. */
    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Boolean = false,
)

fun NoteEntity.asExternalModel(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isPinned = isPinned,
)

fun Note.asEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isPinned = isPinned,
)
