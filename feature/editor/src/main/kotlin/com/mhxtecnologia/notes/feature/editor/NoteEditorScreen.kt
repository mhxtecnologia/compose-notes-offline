package com.mhxtecnologia.notes.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhxtecnologia.notes.core.designsystem.theme.NotesTheme

@Composable
internal fun NoteEditorRoute(
    onFinished: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NoteEditorScreen(uiState = uiState, onEvent = viewModel::onEvent, onFinished = onFinished)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteEditorScreen(
    uiState: NoteEditorUiState,
    onEvent: (NoteEditorEvent) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) currentOnFinished()
    }

    // O voltar do sistema (gesto ou botão) passa pelo mesmo caminho da seta da barra.
    BackHandler(enabled = !uiState.isFinished) { onEvent(NoteEditorEvent.SaveAndClose) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleRes = if (uiState.isNewNote) {
                        R.string.feature_editor_new_note
                    } else {
                        R.string.feature_editor_edit_note
                    }
                    Text(text = stringResource(titleRes), modifier = Modifier.semantics { heading() })
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(NoteEditorEvent.SaveAndClose) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_editor_save_and_close),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            val description = stringResource(R.string.feature_editor_loading)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.semantics { contentDescription = description })
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { onEvent(NoteEditorEvent.TitleChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.feature_editor_title)) },
                    textStyle = MaterialTheme.typography.titleLarge,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { onEvent(NoteEditorEvent.ContentChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 12.dp),
                    label = { Text(stringResource(R.string.feature_editor_content)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteEditorScreenPreview() {
    NotesTheme(dynamicColor = false) {
        NoteEditorScreen(
            uiState = NoteEditorUiState(
                title = "Groceries",
                content = "Milk, eggs, coffee beans and bread for the weekend.",
                isNewNote = false,
            ),
            onEvent = {},
            onFinished = {},
        )
    }
}
