package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String = "General",
    val title: String,
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis() + 86400000L * 2,
    val priority: String = "Medium", // High, Medium, Low
    val isCompleted: Boolean = false,
    val estimatedMinutes: Int = 45,
    val subtasks: String = "" // newline separated subtasks
)
