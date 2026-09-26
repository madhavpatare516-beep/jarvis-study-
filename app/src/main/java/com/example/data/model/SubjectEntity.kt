package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String = "#38BDF8",
    val targetWeeklyHours: Float = 5.0f,
    val instructor: String = "",
    val roomOrLocation: String = "",
    val syllabusTopics: String = "", // newline-delimited topics
    val completedTopics: String = "" // newline-delimited completed topics
)
