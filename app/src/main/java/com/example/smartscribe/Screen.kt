// File: app/src/main/java/com/example/smartscribe/Screen.kt
package com.example.smartscribe

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NotesList : Screen("notes")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }
}