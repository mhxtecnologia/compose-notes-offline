package com.mhxtecnologia.notes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Banco local do app. `exportSchema = true` grava cada versão em `schemas/`,
 * e toda mudança de versão exige uma migration testada: o app guarda dados
 * que o usuário não consegue recriar, então nada de migração destrutiva.
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val NAME = "notes.db"
    }
}
