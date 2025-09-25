package com.example.smartscribe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val summary: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val category: String = "General", // Meeting, Lecture, Personal, etc.
    val wordCount: Int = 0
)