// File: app/src/main/java/com/example/smartscribe/MainActivity.kt
package com.example.smartscribe

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartscribe.ui.NoteDetailScreen
import com.example.smartscribe.ui.NotesListScreen
import com.example.smartscribe.ui.SpeechToTextScreen
import com.example.smartscribe.ui.theme.SmartScribeTheme
import com.example.smartscribe.ui.theme.ThemeMode

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
            SmartScribeApp()
        }
    }
}

@Composable
fun SmartScribeApp() {
    // Remember theme mode across app restarts
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }

    SmartScribeTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Screen.NotesList.route
            ) {
                composable(Screen.Home.route) {
                    SpeechToTextScreen(
                        onNavigateToNotes = {
                            navController.navigate(Screen.NotesList.route)
                        }
                    )
                }
                composable(Screen.NotesList.route) {
                    NotesListScreen(
                        onNoteClick = { noteId ->
                            navController.navigate(Screen.NoteDetail.createRoute(noteId))
                        },
                        onNavigateToSpeech = {
                            navController.navigate(Screen.Home.route)
                        },
                        onThemeModeChanged = { newMode ->
                            themeMode = newMode
                        },
                        currentThemeMode = themeMode
                    )
                }
                composable(
                    route = Screen.NoteDetail.route,
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
                    NoteDetailScreen(
                        noteId = noteId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}