package com.mhxtecnologia.notes.feature.editor

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mhxtecnologia.notes.core.designsystem.theme.NotesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val events = mutableListOf<NoteEditorEvent>()
    private var finished = false

    @Test
    fun showsTheTextOfTheNote() {
        setContent(NoteEditorUiState(title = "Groceries", content = "Milk", isNewNote = false))

        composeTestRule.onNodeWithText(context.getString(R.string.feature_editor_edit_note)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk").assertIsDisplayed()
    }

    @Test
    fun typingInTheTitleSendsTitleChanged() {
        setContent(NoteEditorUiState())

        composeTestRule
            .onNode(hasSetTextAction() and hasText(context.getString(R.string.feature_editor_title)))
            .performTextInput("Ideas")

        assertEquals(listOf<NoteEditorEvent>(NoteEditorEvent.TitleChanged("Ideas")), events)
    }

    @Test
    fun backButtonHasAnAccessibleNameAndSaves() {
        setContent(NoteEditorUiState())

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.feature_editor_save_and_close))
            .performClick()

        assertEquals(listOf<NoteEditorEvent>(NoteEditorEvent.SaveAndClose), events)
    }

    @Test
    fun finishedStateNavigatesBack() {
        setContent(NoteEditorUiState(isFinished = true))

        composeTestRule.waitForIdle()

        assertEquals(true, finished)
    }

    @Test
    fun loadingIndicatorIsAnnouncedToScreenReaders() {
        setContent(NoteEditorUiState(isLoading = true, isNewNote = false))

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.feature_editor_loading))
            .assertIsDisplayed()
    }

    private fun setContent(uiState: NoteEditorUiState) {
        composeTestRule.setContent {
            NotesTheme(dynamicColor = false) {
                NoteEditorScreen(
                    uiState = uiState,
                    onEvent = { events += it },
                    onFinished = { finished = true },
                )
            }
        }
    }
}
