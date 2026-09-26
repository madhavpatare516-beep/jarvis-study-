package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long = 0,
    val subjectName: String,
    val subjectColorHex: String = "#38BDF8",
    val title: String,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val startTime: String, // "09:00"
    val endTime: String,   // "10:30"
    val studyType: String = "Lecture", // Lecture, Lab, Self-Study, Revision, Tutorial
    val location: String = "" // "Hall A", "Library", "Desk"
)
