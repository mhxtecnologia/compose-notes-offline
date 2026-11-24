package com.mhxtecnologia.notes.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: notas fixadas. Só acrescenta (coluna com DEFAULT 0 e um índice), então
 * toda nota existente continua igual e entra como "não fixada". Nenhuma linha é
 * apagada ou recriada; o MigrationTest prova isso a cada build.
 */
internal object Migration1To2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `notes` ADD COLUMN `is_pinned` INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_notes_is_pinned_updated_at` ON `notes` (`is_pinned`, `updated_at`)",
        )
    }
}
