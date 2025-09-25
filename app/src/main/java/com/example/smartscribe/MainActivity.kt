package com.example.smartscribe

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import com.example.smartscribe.data.AppDatabase
import com.example.smartscribe.data.NoteRepository
import java.util.Date

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Handle permission denial if needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpeechToTextScreen()
                }
            }
        }
    }
}

@Composable
fun SpeechToTextScreen() {
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf(TextFieldValue("Press the button to start speaking...")) }
    var summaryText by remember { mutableStateOf("Summary will appear here...") }
    var isListening by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var aiModelStatus by remember { mutableStateOf("🤖 AI Ready") }
    var saveStatus by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Room Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val noteRepository = remember { NoteRepository(database.noteDao()) }

    // Scroll state for summary section
    val scrollState = rememberScrollState()

    // Speech Recognizer
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Save Status
        if (saveStatus.isNotEmpty()) {
            Text(
                text = saveStatus,
                color = if (saveStatus.contains("✅")) Color.Green else Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // AI Status Indicator
        Text(
            text = aiModelStatus,
            color = when {
                aiModelStatus.contains("Ready") -> Color.Green
                aiModelStatus.contains("Processing") -> Color.Blue
                else -> Color.Gray
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
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        } else if (isLoading) {
            Text(
                text = "⏳ AI is analyzing your content...",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // Error Message
        errorMessage?.let { message ->
            Text(
                text = "⚠️ $message",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // Recognized Speech Section
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
                saveStatus = "" // Clear save status when editing

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
                    summaryText = "Enter text or speak to get AI summary"
                } else {
                    summaryText = "Type more content (30+ characters) for AI analysis..."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = false,
            maxLines = 10,
            placeholder = {
                Text("Speak or type your content here...")
            }
        )

        // Summary Section
        Text(
            text = "🤖 AI Summary:",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Box(
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
            Text(
                text = summaryText,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NEW: Save Note Button
        Button(
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
                            saveStatus = "✅ Note saved successfully! (Category: $category)"
                        } catch (e: Exception) {
                            saveStatus = "❌ Error saving note: ${e.message}"
                        }
                    }
                } else {
                    saveStatus = "❌ Please add content before saving"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE) // Purple color for save button
            )
        ) {
            Text(
                text = "💾 Save Note to Database",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control Button
        Button(
            onClick = {
                when {
                    isLoading -> return@Button
                    isListening -> {
                        speechRecognizer.stopListening()
                        recognizedText = TextFieldValue("Stopping...")
                    }
                    else -> {
                        try {
                            speechRecognizer.startListening(speechIntent)
                            isListening = true
                            errorMessage = null
                            summaryText = "AI will analyze your speech..."
                            aiModelStatus = "🎤 Listening..."
                        } catch (e: Exception) {
                            errorMessage = "Failed to start: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    isLoading -> Color(0xFF2196F3)
                    isListening -> Color(0xFFF44336)
                    else -> Color(0xFF4CAF50)
                }
            ),
            enabled = !isLoading
        ) {
            Text(
                text = when {
                    isLoading -> "⏳ AI Processing..."
                    isListening -> "🎤 Stop Listening"
                    else -> "🎤 Start AI Listening"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Help Text
        Text(
            text = when {
                isListening -> "💡 Speak clearly. AI will analyze and summarize automatically."
                isLoading -> "💡 AI is generating intelligent summary..."
                else -> "Create content above, then save it to your personal database"
            },
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

// Helper functions for note management
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

// Keep all your existing AI functions below (generateIntelligentSummary, etc.)
// ... [ALL YOUR EXISTING AI FUNCTIONS REMAIN THE SAME] ...
// Improved AI summarization function
private fun generateIntelligentSummary(text: String): String {
    val sentences = text.split(". ", "! ", "? ").filter { it.isNotBlank() }
    val words = text.split(" ").filter { it.isNotBlank() }

    if (sentences.size <= 1 || words.size < 10) {
        return "📝 Content too short for meaningful summary. Please provide more text."
    }

    return when {
        // Meeting detection - improved
        isMeetingContent(text) -> generateMeetingSummary(text)

        // Educational content - improved
        isEducationalContent(text) -> generateEducationalSummary(text)

        // Story/narrative content
        isNarrativeContent(text) -> generateNarrativeSummary(text)

        // Technical/content-heavy
        isTechnicalContent(text) -> generateTechnicalSummary(text)

        // General content - much improved!
        else -> generateGeneralSummary(text)
    }
}

// Improved content type detection
private fun isMeetingContent(text: String): Boolean {
    val meetingKeywords = listOf("meeting", "discuss", "agenda", "minutes", "present", "team", "project", "decision")
    return meetingKeywords.any { text.contains(it, ignoreCase = true) } && text.length > 50
}

private fun isEducationalContent(text: String): Boolean {
    val eduKeywords = listOf("chapter", "lesson", "learn", "study", "explain", "concept", "theory", "example")
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

// Improved summary generators
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

// Helper functions
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
    // Simple extraction of potential names (words starting with capital letter, not at beginning of sentence)
    val words = text.split(" ")
    return words.filterIndexed { index, word ->
        word.length > 2 && word[0].isUpperCase() && index > 0 &&
                !word[0].isLowerCase() && words[index-1].let { !it.endsWith(".") && !it.endsWith("!") && !it.endsWith("?") }
    }.distinct()
}
private fun generateNarrativeSummary(text: String): String {
    val sentences = text.split(". ", "! ", "? ").filter { it.isNotBlank() }
    // Keywords might represent characters or key themes
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
// Keep the existing functions unchanged below this line...
// Keep the existing helper functions unchanged
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
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
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