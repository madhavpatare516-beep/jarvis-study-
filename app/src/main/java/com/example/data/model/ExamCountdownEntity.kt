package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_countdowns")
data class ExamCountdownEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val title: String,
    val targetDate: Long, // timestamp
    val weightPercent: Int = 25, // %
    val preparednessScore: Int = 70 // 0 to 100%
)
