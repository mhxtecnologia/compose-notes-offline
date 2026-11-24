package com.mhxtecnologia.notes.feature.notes

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mhxtecnologia.notes.core.designsystem.theme.NotesTheme
import com.mhxtecnologia.notes.core.model.Note
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Testa a tela sem estado: entra um UiState, saem eventos e callbacks. */
@RunWith(AndroidJUnit4::class)
class NotesListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val events = mutableListOf<NotesListEvent>()
    private var openedNoteId: Long? = null
    private var newNoteClicks = 0

    private val groceries = note(1, "Groceries", "Milk and eggs")
    private val trip = note(2, "Trip", "Book the hotel")

    @Test
    fun showsEveryNoteWithItsPreview() {
        setContent(NotesListUiState(notes = listOf(groceries, trip), isLoading = false))

        composeTestRule.onNodeWithText("Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk and eggs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trip").assertIsDisplayed()
    }

    @Test
    fun noteWithoutTitleIsShownAsUntitled() {
        setContent(NotesListUiState(notes = listOf(note(3, "", "Just text")), isLoading = false))

        composeTestRule.onNodeWithText(string(R.string.feature_notes_untitled)).assertIsDisplayed()
    }

    @Test
    fun showsEmptyStateWhenThereAreNoNotes() {
        setContent(NotesListUiState(isLoading = false))

        composeTestRule.onNodeWithText(string(R.string.feature_notes_empty)).assertIsDisplayed()
    }

    @Test
    fun showsNoResultsMessageForASearchWithoutMatches() {
        setContent(NotesListUiState(query = "zzz", isLoading = false))

        composeTestRule.onNodeWithText(string(R.string.feature_notes_no_results, "zzz")).assertIsDisplayed()
    }

    @Test
    fun loadingIndicatorIsAnnouncedToScreenReaders() {
        setContent(NotesListUiState(isLoading = true))

        composeTestRule.onNodeWithContentDescription(string(R.string.feature_notes_loading)).assertIsDisplayed()
    }

    @Test
    fun tappingANoteOpensIt() {
        setContent(NotesListUiState(notes = listOf(groceries, trip), isLoading = false))

        composeTestRule.onNodeWithText("Trip").performClick()

        assertEquals(2L, openedNoteId)
    }

    @Test
    fun deleteButtonNamesTheNoteAndSendsTheEvent() {
        setContent(NotesListUiState(notes = listOf(groceries, trip), isLoading = false))

        composeTestRule
            .onNodeWithContentDescription(string(R.string.feature_notes_delete, "Groceries"))
            .performClick()

        assertEquals(listOf<NotesListEvent>(NotesListEvent.DeleteNote(groceries)), events)
    }

    @Test
    fun pinButtonIsAToggleThatReflectsTheNoteState() {
        val pinnedTrip = trip.copy(isPinned = true)
        setContent(NotesListUiState(notes = listOf(pinnedTrip, groceries), isLoading = false))

        composeTestRule.onNodeWithContentDescription(string(R.string.feature_notes_pin, "Trip")).assertIsOn()
        composeTestRule
            .onNodeWithContentDescription(string(R.string.feature_notes_pin, "Groceries"))
            .assertIsOff()
            .performClick()

        assertEquals(listOf<NotesListEvent>(NotesListEvent.TogglePin(groceries)), events)
    }

    @Test
    fun typingInTheSearchFieldSendsQueryChanged() {
        setContent(NotesListUiState(isLoading = false))

        composeTestRule.onNode(hasSetTextAction()).performTextInput("milk")

        assertEquals(listOf<NotesListEvent>(NotesListEvent.QueryChanged("milk")), events)
    }

    @Test
    fun newNoteButtonHasAnAccessibleName() {
        setContent(NotesListUiState(isLoading = false))

        composeTestRule.onNodeWithContentDescription(string(R.string.feature_notes_new_note)).performClick()

        assertEquals(1, newNoteClicks)
    }

    private fun setContent(uiState: NotesListUiState) {
        composeTestRule.setContent {
            NotesTheme(dynamicColor = false) {
                NotesListScreen(
                    uiState = uiState,
                    onEvent = { events += it },
                    onNoteClick = { openedNoteId = it },
                    onNewNote = { newNoteClicks++ },
                )
            }
        }
    }

    private fun string(@StringRes id: Int): String = context.getString(id)

    private fun string(@StringRes id: Int, arg: String): String = context.getString(id, arg)

    private fun note(id: Long, title: String, content: String) = Note(
        id = id,
        title = title,
        content = content,
        createdAt = Instant.parse("2026-11-20T10:00:00Z"),
        updatedAt = Instant.parse("2026-11-20T10:00:00Z"),
    )
}
