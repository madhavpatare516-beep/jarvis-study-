package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val studyMode: String = "Pomodoro", // Pomodoro, Deep Focus, Sprint, Stopwatch
    val notes: String = "",
    val productivityRating: Int = 5 // 1 to 5 stars
)
