package com.mhxtecnologia.notes.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prova que atualizar o app não perde nota nenhuma. O MigrationTestHelper cria o
 * banco exatamente como a v1 era (a partir de schemas/.../1.json), grava dados
 * com SQL cru, roda a migration e confere o esquema contra o 2.json.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NotesDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsEveryNoteAndMarksThemAsNotPinned() {
        helper.createDatabase(TEST_DB, 1).use { db -> insertVersion1Notes(db) }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration1To2)

        migrated.query(
            "SELECT id, title, content, created_at, updated_at, is_pinned FROM notes ORDER BY id",
        ).use { cursor ->
            assertEquals(2, cursor.count)

            cursor.moveToNext()
            assertEquals(1L, cursor.getLong(0))
            assertEquals("Groceries", cursor.getString(1))
            assertEquals("Milk, eggs", cursor.getString(2))
            assertEquals(1_000L, cursor.getLong(3))
            assertEquals(2_000L, cursor.getLong(4))
            assertEquals(0, cursor.getInt(5))

            cursor.moveToNext()
            assertEquals(2L, cursor.getLong(0))
            assertEquals("Trip", cursor.getString(1))
            assertEquals("", cursor.getString(2))
            assertEquals(0, cursor.getInt(5))
        }
        migrated.close()
    }

    /**
     * Além do JSON, valida contra o código: abre o banco migrado com o Room de
     * verdade, que confere o esquema das entidades compiladas e lê pelo DAO.
     */
    @Test
    fun migratedDatabase_opensWithRoomAndReadsTheOldNotes() = runTest {
        helper.createDatabase(TEST_DB, 1).use { db -> insertVersion1Notes(db) }

        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            NotesDatabase::class.java,
            TEST_DB,
        )
            .addMigrations(Migration1To2)
            .build()
        helper.closeWhenFinished(database)

        val notes = database.noteDao().observeNotes(pattern = "").first()

        assertEquals(listOf("Trip", "Groceries"), notes.map { it.title })
        assertEquals(listOf(false, false), notes.map { it.isPinned })
    }

    private fun insertVersion1Notes(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO notes (id, title, content, created_at, updated_at) " +
                "VALUES (1, 'Groceries', 'Milk, eggs', 1000, 2000)",
        )
        db.execSQL(
            "INSERT INTO notes (id, title, content, created_at, updated_at) " +
                "VALUES (2, 'Trip', '', 3000, 4000)",
        )
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
