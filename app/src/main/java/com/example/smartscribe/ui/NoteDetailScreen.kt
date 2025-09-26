package com.example.smartscribe.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smartscribe.data.AppDatabase
import com.example.smartscribe.data.NoteRepository
import kotlinx.coroutines.launch
import com.example.smartscribe.data.Note
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { NoteRepository(database.noteDao()) }

    var note by remember { mutableStateOf<Note?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    // Load note
    LaunchedEffect(noteId) {
        repository.getNoteById(noteId)?.let {
            note = it
            title = it.title
            content = it.content
            summary = it.summary
            category = it.category
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Note" else "Note Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        // Save button
                        // Inside the "Save" IconButton click handler
                        IconButton(onClick = {
                            note?.let { currentNote ->  // 👈 SAFE unwrap
                                coroutineScope.launch {
                                    repository.updateNote(
                                        currentNote.copy(  // ✅ now non-null
                                            title = title,
                                            content = content,
                                            summary = summary,
                                            category = category
                                        )
                                    )
                                    isEditing = false
                                }
                            }
                        }
                        )
                        {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    } else {
                        // Edit & Share
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = {
                            // Share as plain text
                            val shareText = """
                                📝 ${title}

                                $content

                                🤖 AI Summary:
                                $summary
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        // Optional: Delete
                        IconButton(onClick = {
                            coroutineScope.launch {
                                note?.let { repository.deleteNote(it) }
                                onBack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        note?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        maxLines = 12
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("AI Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "🤖 AI Summary",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Category: $category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}