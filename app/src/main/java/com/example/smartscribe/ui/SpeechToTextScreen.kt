// File: app/src/main/java/com/example/smartscribe/ui/SpeechToTextScreen.kt
package com.example.smartscribe.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartscribe.data.AppDatabase
import com.example.smartscribe.data.NoteRepository
import com.example.smartscribe.ui.utils.HapticType
import com.example.smartscribe.ui.utils.rememberHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechToTextScreen(
    onNavigateToNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf(TextFieldValue("Press the mic to start speaking...")) }
    var summaryText by remember { mutableStateOf("Summary will appear here...") }
    var isListening by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var aiModelStatus by remember { mutableStateOf("🤖 AI Ready") }
    var saveStatus by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context) }
    val noteRepository = remember { NoteRepository(database.noteDao()) }
    val scrollState = rememberScrollState()

    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener(
                onResult = { text ->
                    recognizedText = TextFieldValue(text)
                    isListening = false
                    isLoading = true
                    aiModelStatus = "🧠 AI Processing..."
                    coroutineScope.launch {
                        summaryText = generateIntelligentSummary(text)
                        isLoading = false
                        aiModelStatus = "🤖 AI Ready"
                        scrollState.scrollTo(0)
                    }
                },
                onError = { error ->
                    handleSpeechError(error, onError = { message ->
                        errorMessage = message
                        isListening = false
                        isLoading = false
                        aiModelStatus = "🤖 AI Ready"
                    })
                },
                onReady = {
                    errorMessage = null
                    recognizedText = TextFieldValue("Listening... Speak now!")
                },
                onBeginning = {
                    recognizedText = TextFieldValue("Speak now...")
                }
            ))
        }
    }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartScribe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToNotes) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Notes")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Save
                    IconButton(
                        onClick = {
                            if (recognizedText.text.isNotEmpty() && summaryText.isNotEmpty()) {
                                coroutineScope.launch {
                                    try {
                                        val title = generateNoteTitle(recognizedText.text)
                                        val category = detectNoteCategory(recognizedText.text)
                                        noteRepository.insertNote(
                                            title = title,
                                            content = recognizedText.text,
                                            summary = summaryText,
                                            category = category
                                        )
                                        saveStatus = "✅ Saved! ($category)"
                                        haptic(HapticType.SUCCESS)
                                    } catch (e: Exception) {
                                        saveStatus = "❌ Error: ${e.message}"
                                        haptic(HapticType.ERROR)
                                    }
                                }
                            } else {
                                saveStatus = "❌ Add content first"
                                haptic(HapticType.ERROR)
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (!isLoading) Color(0xFF6200EE) else Color.Gray
                        )
                    }

                    // Mic
                    IconButton(
                        onClick = {
                            when {
                                isLoading -> return@IconButton
                                isListening -> {
                                    speechRecognizer.stopListening()
                                    recognizedText = TextFieldValue("Stopping...")
                                    haptic(HapticType.LIGHT)
                                }
                                else -> {
                                    try {
                                        speechRecognizer.startListening(speechIntent)
                                        isListening = true
                                        errorMessage = null
                                        summaryText = "AI will analyze your speech..."
                                        aiModelStatus = "🎤 Listening..."
                                        haptic(HapticType.MEDIUM)
                                    } catch (e: Exception) {
                                        errorMessage = "Failed: ${e.message}"
                                        haptic(HapticType.ERROR)
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop" else "Start",
                            tint = when {
                                isLoading -> Color.Gray
                                isListening -> Color(0xFFF44336)
                                else -> Color(0xFF4CAF50)
                            }
                        )
                    }

                    // Home
                    IconButton(onClick = {
                        haptic(HapticType.SELECTION)
                        onNavigateToNotes()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // AI (grayed out)
                    IconButton(onClick = { /* Future use */ }) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = "AI",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Save Status
            if (saveStatus.isNotEmpty()) {
                Text(
                    text = saveStatus,
                    color = if (saveStatus.contains("✅")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // AI Status
            Text(
                text = aiModelStatus,
                color = when {
                    aiModelStatus.contains("Ready") -> MaterialTheme.colorScheme.primary
                    aiModelStatus.contains("Processing") -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Status Header
            if (isListening) {
                Text(
                    text = "🔴 LIVE - Recording",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            } else if (isLoading) {
                Text(
                    text = "⏳ AI is analyzing...",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Error
            errorMessage?.let { message ->
                Text(
                    text = "⚠️ $message",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Content
            Text(
                text = "Your Content:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = recognizedText,
                onValueChange = { newTextValue ->
                    recognizedText = newTextValue
                    saveStatus = ""
                    if (newTextValue.text.length > 30) {
                        isLoading = true
                        aiModelStatus = "🧠 AI Processing..."
                        coroutineScope.launch {
                            delay(1000)
                            if (newTextValue.text == recognizedText.text) {
                                summaryText = generateIntelligentSummary(newTextValue.text)
                                isLoading = false
                                aiModelStatus = "🤖 AI Ready"
                                scrollState.scrollTo(0)
                            }
                        }
                    } else if (newTextValue.text.isEmpty()) {
                        summaryText = "Speak or type to get AI summary"
                    } else {
                        summaryText = "Type more (30+ chars) for AI analysis..."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = false,
                maxLines = 10,
                placeholder = { Text("Speak or type your content here...") }
            )

            // Summary
            Text(
                text = "🤖 AI Summary:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                AnimatedVisibility(
                    visible = shouldAnimateSummary(summaryText),
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    Text(
                        text = summaryText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Help Text
            Text(
                text = when {
                    isListening -> "💡 Speak clearly. AI will summarize automatically."
                    isLoading -> "💡 AI is generating summary..."
                    else -> "Tap mic to start or type above"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

private fun shouldAnimateSummary(text: String): Boolean {
    return text.isNotEmpty() &&
            text != "Summary will appear here..." &&
            text != "Speak or type to get AI summary" &&
            text != "Enter text or speak to get AI summary" &&
            !text.startsWith("Type more content") &&
            !text.startsWith("📝 Content too short")
}

// =============== ALL YOUR EXISTING HELPER FUNCTIONS BELOW ===============

private fun generateNoteTitle(content: String): String {
    val firstSentence = content.split(". ").firstOrNull() ?: "New Note"
    return if (firstSentence.length > 40) {
        firstSentence.take(40) + "..."
    } else {
        firstSentence
    }
}

private fun detectNoteCategory(content: String): String {
    return when {
        content.contains("meeting", ignoreCase = true) -> "Meeting"
        content.contains("lecture", ignoreCase = true) || content.contains("chapter", ignoreCase = true) -> "Lecture"
        content.contains("shopping", ignoreCase = true) || content.contains("buy", ignoreCase = true) -> "Shopping"
        content.contains("idea", ignoreCase = true) || content.contains("project", ignoreCase = true) -> "Ideas"
        content.length > 200 -> "Long Form"
        else -> "General"
    }
}

private fun generateIntelligentSummary(text: String): String {
    val sentences = text.split(". ", "! ", "? ").filter { it.isNotBlank() }
    val words = text.split(" ").filter { it.isNotBlank() }

    if (sentences.size <= 1 || words.size < 10) {
        return "📝 Content too short for meaningful summary. Please provide more text."
    }

    return when {
        isMeetingContent(text) -> generateMeetingSummary(text)
        isEducationalContent(text) -> generateEducationalSummary(text)
        isNarrativeContent(text) -> generateNarrativeSummary(text)
        isTechnicalContent(text) -> generateTechnicalSummary(text)
        else -> generateGeneralSummary(text)
    }
}

private fun isMeetingContent(text: String): Boolean {
    val meetingKeywords = listOf("meeting", "discuss", "agenda", "minutes", "present", "team", "project", "decision")
    return meetingKeywords.any { text.contains(it, ignoreCase = true) } && text.length > 50
}

private fun isEducationalContent(text: String): Boolean {
    val eduKeywords = listOf("chapter", "lesson", "learn", "study", "explain", "concept", " theory", "example")
    return eduKeywords.any { text.contains(it, ignoreCase = true) } || text.length > 200
}

private fun isNarrativeContent(text: String): Boolean {
    val narrativeKeywords = listOf("story", "narrative", "told", "said", "explained", "described", "event", "experience")
    return narrativeKeywords.any { text.contains(it, ignoreCase = true) }
}

private fun isTechnicalContent(text: String): Boolean {
    val techKeywords = listOf("code", "program", "function", "method", "algorithm", "system", "technical", "implementation")
    return techKeywords.any { text.contains(it, ignoreCase = true) }
}

private fun generateMeetingSummary(text: String): String {
    val sentences = text.split(". ", "! ", "? ").filter { it.isNotBlank() }
    val decisions = sentences.filter {
        it.contains("decide", ignoreCase = true) ||
                it.contains("agree", ignoreCase = true) ||
                it.contains("conclude", ignoreCase = true)
    }
    val actions = sentences.filter {
        it.contains("will", ignoreCase = true) ||
                it.contains("need to", ignoreCase = true) ||
                it.contains("action", ignoreCase = true)
    }

    return "📅 Meeting Summary:\n" +
            "• Topic: ${extractMainTopic(text)}\n" +
            "• Key decisions: ${if (decisions.isNotEmpty()) decisions.take(2).joinToString("; ") else "None specified"}\n" +
            "• Action items: ${if (actions.isNotEmpty()) actions.take(3).joinToString("; ") else "No specific actions"}\n" +
            "• Participants mentioned: ${extractParticipants(text).take(3).joinToString(", ")}"
}

private fun generateEducationalSummary(text: String): String {
    val keyConcepts = extractKeywords(text, 5)
    val mainPoints = text.split(". ").filter { sentence ->
        sentence.length > 20 && keyConcepts.any { sentence.contains(it, ignoreCase = true) }
    }.take(3)

    return "📚 Educational Summary:\n" +
            "• Main topic: ${extractMainTopic(text)}\n" +
            "• Key concepts: ${keyConcepts.joinToString(", ")}\n" +
            "• Important points:\n   - ${mainPoints.joinToString("\n   - ")}"
}

private fun generateGeneralSummary(text: String): String {
    val sentences = text.split(". ", "! ", "? ").filter { it.isNotBlank() }
    val keyPoints = sentences.filter { it.length > 15 }
        .sortedByDescending { it.length }
        .take(3)

    val mainIdea = if (sentences.size >= 2) sentences[1] else sentences.firstOrNull() ?: ""

    return "🤖 AI Analysis:\n" +
            "• Main idea: ${mainIdea.take(80)}${if (mainIdea.length > 80) "..." else ""}\n" +
            "• Key points:\n   - ${keyPoints.joinToString("\n   - ") { it.take(100) + if (it.length > 100) "..." else "" }}"
}

private fun generateNarrativeSummary(text: String): String {
    val sentences = text.split(". ", "! ", "? ").filter { it.isNotBlank() }
    val keywords = extractKeywords(text, 3)
    val keyEvents = sentences.filter {
        val actionVerbs = listOf("went", "found", "decided", "created", "began")
        it.length > 15 && actionVerbs.any { verb -> it.contains(verb, ignoreCase = true) }
    }.take(3)

    return "📖 Narrative Summary:\n" +
            "• Main Theme/Characters: ${keywords.joinToString(", ")}\n" +
            "• Key Events:\n   - ${if (keyEvents.isNotEmpty()) keyEvents.joinToString("\n   - ") else "No specific events found."}"
}

private fun generateTechnicalSummary(text: String): String {
    val techKeywords = extractKeywords(text, 5)
    val definitions = text.split(". ").filter { sentence ->
        sentence.length > 15 && techKeywords.any { sentence.contains(it, ignoreCase = true) }
    }.take(3)

    return "⚙️ Technical Summary:\n" +
            "• Core Topic: ${extractMainTopic(text)}\n" +
            "• Key Terms: ${techKeywords.joinToString(", ")}\n" +
            "• Main Points:\n   - ${if (definitions.isNotEmpty()) definitions.joinToString("\n   - ") else "No specific points found."}"
}

private fun extractMainTopic(text: String): String {
    val firstSentence = text.split(". ").firstOrNull() ?: ""
    return firstSentence.take(60) + if (firstSentence.length > 60) "..." else ""
}

private fun extractKeywords(text: String, count: Int): List<String> {
    val commonWords = setOf("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "is", "are", "was", "were")
    val words = text.split(" ").filter {
        it.length > 4 && it !in commonWords && it[0].isUpperCase()
    }
    val frequency = words.groupingBy { it.lowercase() }.eachCount()
    return frequency.entries.sortedByDescending { it.value }.take(count).map { it.key }
}

private fun extractParticipants(text: String): List<String> {
    val words = text.split(" ")
    return words.filterIndexed { index, word ->
        word.length > 2 && word[0].isUpperCase() && index > 0 &&
                !word[0].isLowerCase() && words[index-1].let { !it.endsWith(".") && !it.endsWith("!") && !it.endsWith("?") }
    }.distinct()
}

private fun createRecognitionListener(
    onResult: (String) -> Unit,
    onError: (Int) -> Unit,
    onReady: () -> Unit,
    onBeginning: () -> Unit
): android.speech.RecognitionListener {
    return object : android.speech.RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = onReady()
        override fun onBeginningOfSpeech() = onBeginning()
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) = onError(error)
        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: "No speech recognized"
            onResult(text)
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}

private fun handleSpeechError(error: Int, onError: (String) -> Unit) {
    val message = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please try again."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timeout. Please try again."
        SpeechRecognizer.ERROR_AUDIO -> "Audio error. Please check microphone."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition error. Restarting..."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
        SpeechRecognizer.ERROR_NETWORK -> "Network error. Check internet connection."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Check internet."
        SpeechRecognizer.ERROR_SERVER -> "Server error. Please try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition busy. Please wait."
        else -> "Unknown error: $error"
    }
    onError(message)
}