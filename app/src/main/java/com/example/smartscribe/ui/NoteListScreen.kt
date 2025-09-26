// File: app/src/main/java/com/example/smartscribe/ui/NotesListScreen.kt
package com.example.smartscribe.ui

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartscribe.data.AppDatabase
import com.example.smartscribe.data.Note
import com.example.smartscribe.data.NoteRepository
import com.example.smartscribe.ui.components.ThemeToggle
import com.example.smartscribe.ui.theme.ThemeMode
import com.example.smartscribe.ui.utils.HapticType
import com.example.smartscribe.ui.utils.rememberHapticFeedback
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (Long) -> Unit,
    onNavigateToSpeech: () -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit, // 👈 Added parameter
    currentThemeMode: ThemeMode, // 👈 Added parameter
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { NoteRepository(database.noteDao()) }

    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }

    LaunchedEffect(searchQuery, selectedCategory) {
        val flow = when {
            searchQuery.isNotBlank() -> repository.searchNotes(searchQuery)
            selectedCategory != null -> repository.getNotesByCategory(selectedCategory!!)
            else -> repository.allNotes
        }
        flow.collectLatest { noteList ->
            notes = noteList
        }
    }

    var allCategories by remember { mutableStateOf(listOf("General")) }
    LaunchedEffect(Unit) {
        repository.allNotes.collectLatest { list ->
            allCategories = list.map { it.category }.distinct()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Notes") },
                actions = {
                    // 👇 Theme Toggle (left of search)
                    ThemeToggle(
                        currentMode = currentThemeMode,
                        onModeChange = onThemeModeChanged
                    )

                    // Search icon
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic(HapticType.MEDIUM)
                    onNavigateToSpeech()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Note",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .onFocusChanged { focusState ->
                            if (focusState.hasFocus) {
                                haptic(HapticType.LIGHT)
                            }
                        }
                        .focusable(interactionSource = interactionSource),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = {
                                haptic(HapticType.SELECTION)
                                selectedCategory = null
                            },
                            label = { Text("All") }
                        )
                    }
                    items(allCategories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                haptic(HapticType.SELECTION)
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                            "No matching notes"
                        } else {
                            "No notes yet.\nTap + to create one!"
                        },
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(notes) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                haptic(HapticType.SELECTION)
                                onNoteClick(note.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = note.summary.take(100) + if (note.summary.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.category,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        note.updatedAt.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}