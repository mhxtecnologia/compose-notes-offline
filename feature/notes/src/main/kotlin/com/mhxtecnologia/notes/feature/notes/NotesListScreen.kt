package com.mhxtecnologia.notes.feature.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhxtecnologia.notes.core.designsystem.theme.NotesTheme
import com.mhxtecnologia.notes.core.model.Note
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Liga o ViewModel à tela; a tela em si não conhece o ViewModel. */
@Composable
internal fun NotesListRoute(
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotesListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNoteClick = onNoteClick,
        onNewNote = onNewNote,
    )
}

/** Tela sem estado: recebe o [NotesListUiState] e devolve eventos. Fácil de testar e de pré-visualizar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesListScreen(
    uiState: NotesListUiState,
    onEvent: (NotesListEvent) -> Unit,
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.feature_notes_deleted)
    val undoLabel = stringResource(R.string.feature_notes_undo)
    val currentOnEvent by rememberUpdatedState(onEvent)

    // O "desfazer" é estado (recentlyDeleted), não um evento avulso: sobrevive à
    // rotação e o resultado do snackbar volta para o ViewModel como evento.
    LaunchedEffect(uiState.recentlyDeleted) {
        if (uiState.recentlyDeleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            val undone = result == SnackbarResult.ActionPerformed
            currentOnEvent(if (undone) NotesListEvent.UndoDelete else NotesListEvent.UndoDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.feature_notes_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.feature_notes_new_note))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = { onEvent(NotesListEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                uiState.isLoading -> LoadingState()
                uiState.notes.isEmpty() -> EmptyState(query = uiState.query)
                else -> NotesList(
                    notes = uiState.notes,
                    onNoteClick = onNoteClick,
                    onTogglePin = { onEvent(NotesListEvent.TogglePin(it)) },
                    onDelete = { onEvent(NotesListEvent.DeleteNote(it)) },
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        label = { Text(stringResource(R.string.feature_notes_search)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.feature_notes_clear_search))
                }
            }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    onTogglePin: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // Espaço no fim para o FAB não cobrir o botão de apagar da última nota.
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note.id) },
                onTogglePin = { onTogglePin(note) },
                onDelete = { onDelete(note) },
            )
        }
    }
}

/**
 * Cartão de uma nota. A área de texto vira um único alvo de toque, lido pelo
 * leitor de tela como um bloco ("Abrir nota"); fixar e apagar ficam separados,
 * com 48 dp e uma descrição que cita a nota. O fixar é um toggle: o leitor de
 * tela anuncia "ativado/desativado" em vez de trocar o texto do botão.
 */
@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = note.title.ifBlank { stringResource(R.string.feature_notes_untitled) }
    Card(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClickLabel = stringResource(R.string.feature_notes_open),
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .padding(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.content.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.feature_notes_edited, formatTimestamp(note.updatedAt)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.padding(4.dp)) {
                IconToggleButton(checked = note.isPinned, onCheckedChange = { onTogglePin() }) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(R.string.feature_notes_pin, title),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.feature_notes_delete, title),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.feature_notes_loading)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = description })
    }
}

@Composable
private fun EmptyState(query: String, modifier: Modifier = Modifier) {
    val text = if (query.isBlank()) {
        stringResource(R.string.feature_notes_empty)
    } else {
        stringResource(R.string.feature_notes_no_results, query.trim())
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Data e hora no formato do idioma do aparelho (ex.: "24 de nov. de 2026 14:30"). */
@Composable
private fun formatTimestamp(instant: Instant): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }
    return formatter.format(instant)
}

@PreviewLightDark
@Composable
private fun NotesListScreenPreview() {
    NotesTheme(dynamicColor = false) {
        NotesListScreen(
            uiState = NotesListUiState(notes = previewNotes, isLoading = false),
            onEvent = {},
            onNoteClick = {},
            onNewNote = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EmptyNotesListPreview() {
    NotesTheme(dynamicColor = false) {
        NotesListScreen(
            uiState = NotesListUiState(isLoading = false),
            onEvent = {},
            onNoteClick = {},
            onNewNote = {},
        )
    }
}

private val previewNotes = listOf(
    Note(
        id = 1,
        title = "Groceries",
        content = "Milk, eggs, coffee beans and bread for the weekend.",
        createdAt = Instant.parse("2026-11-20T10:15:00Z"),
        updatedAt = Instant.parse("2026-11-24T08:30:00Z"),
        isPinned = true,
    ),
    Note(
        id = 2,
        title = "",
        content = "A note without a title still shows up, labelled as untitled.",
        createdAt = Instant.parse("2026-11-18T19:00:00Z"),
        updatedAt = Instant.parse("2026-11-18T19:00:00Z"),
    ),
)
