package com.example.smartscribe.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insertNote(title: String, content: String, summary: String, category: String = "General"): Long {
        val note = Note(
            title = title,
            content = content,
            summary = summary,
            category = category,
            wordCount = content.split(" ").size
        )
        return noteDao.insertNote(note)
    }
    // In NoteRepository.kt
    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.copy(updatedAt = Date()))
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    fun getNotesByCategory(category: String): Flow<List<Note>> {
        return noteDao.getNotesByCategory(category)
    }

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }
}