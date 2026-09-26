package com.example.data.repository

import com.example.data.local.JarvisStudyDao
import com.example.data.model.ExamCountdownEntity
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class JarvisStudyRepository(private val dao: JarvisStudyDao) {

    // --- Subjects ---
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    suspend fun getSubjectById(id: Long) = dao.getSubjectById(id)
    suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(id: Long) = dao.deleteSubject(id)

    // --- Schedule ---
    val allScheduleItems: Flow<List<ScheduleItemEntity>> = dao.getAllScheduleItems()
    fun getScheduleForDay(dayOfWeek: Int): Flow<List<ScheduleItemEntity>> = dao.getScheduleForDay(dayOfWeek)
    suspend fun insertScheduleItem(item: ScheduleItemEntity) = dao.insertScheduleItem(item)
    suspend fun updateScheduleItem(item: ScheduleItemEntity) = dao.updateScheduleItem(item)
    suspend fun deleteScheduleItem(id: Long) = dao.deleteScheduleItem(id)

    // --- Tasks ---
    val allTasks: Flow<List<StudyTaskEntity>> = dao.getAllTasks()
    suspend fun insertTask(task: StudyTaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: StudyTaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Long) = dao.deleteTask(id)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = dao.setTaskCompleted(id, completed)

    // --- Sessions ---
    val allSessions: Flow<List<StudySessionEntity>> = dao.getAllSessions()
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<StudySessionEntity>> = dao.getSessionsSince(sinceTimestamp)
    suspend fun insertSession(session: StudySessionEntity) = dao.insertSession(session)
    suspend fun deleteSession(id: Long) = dao.deleteSession(id)

    // --- Exams ---
    val allExams: Flow<List<ExamCountdownEntity>> = dao.getAllExams()
    suspend fun insertExam(exam: ExamCountdownEntity) = dao.insertExam(exam)
    suspend fun updateExam(exam: ExamCountdownEntity) = dao.updateExam(exam)
    suspend fun deleteExam(id: Long) = dao.deleteExam(id)

    /**
     * Pre-populates realistic student schedule data if empty
     */
    suspend fun prepopulateIfEmpty() {
        val existingSubjects = dao.getAllSubjects().first()
        if (existingSubjects.isNotEmpty()) return

        // 1. Starter Subjects
        val s1 = SubjectEntity(
            name = "Data Structures & Algorithms",
            code = "CS 210",
            colorHex = "#38BDF8", // Cyan
            targetWeeklyHours = 6.0f,
            instructor = "Prof. Alan Turing",
            roomOrLocation = "Turing Hall 101",
            syllabusTopics = "Asymptotic Notation\nArrays & Linked Lists\nStacks & Queues\nBinary Search Trees\nGraph Traversals (BFS/DFS)\nDynamic Programming",
            completedTopics = "Asymptotic Notation\nArrays & Linked Lists\nStacks & Queues"
        )
        val s2 = SubjectEntity(
            name = "Applied Calculus & Linear Algebra",
            code = "MATH 201",
            colorHex = "#8B5CF6", // Violet
            targetWeeklyHours = 5.0f,
            instructor = "Dr. Katherine Johnson",
            roomOrLocation = "Math Annex 204",
            syllabusTopics = "Matrices & Determinants\nEigenvalues & Vectors\nMultivariable Derivatives\nDouble Integrals\nVector Calculus",
            completedTopics = "Matrices & Determinants\nEigenvalues & Vectors"
        )
        val s3 = SubjectEntity(
            name = "Machine Learning Foundations",
            code = "AI 302",
            colorHex = "#10B981", // Emerald
            targetWeeklyHours = 7.0f,
            instructor = "Dr. Andrew Ng",
            roomOrLocation = "Tech Auditorium",
            syllabusTopics = "Linear & Logistic Regression\nNeural Networks Intro\nGradient Descent\nModel Evaluation & Metrics\nUnsupervised Clustering",
            completedTopics = "Linear & Logistic Regression\nGradient Descent"
        )
        val s4 = SubjectEntity(
            name = "Computer Systems & Architecture",
            code = "CS 230",
            colorHex = "#F59E0B", // Amber
            targetWeeklyHours = 4.5f,
            instructor = "Prof. John von Neumann",
            roomOrLocation = "Hardware Lab 12",
            syllabusTopics = "Digital Logic Gates\nCPU Pipelining\nCache Hierarchies\nVirtual Memory\nAssembly Language",
            completedTopics = "Digital Logic Gates\nCPU Pipelining"
        )

        val id1 = dao.insertSubject(s1)
        val id2 = dao.insertSubject(s2)
        val id3 = dao.insertSubject(s3)
        val id4 = dao.insertSubject(s4)

        // 2. Schedule Timetable Slots
        // Monday (1)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id1,
            subjectName = "Data Structures & Algorithms",
            subjectColorHex = "#38BDF8",
            title = "CS 210: Binary Search Trees Lecture",
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:30",
            studyType = "Lecture",
            location = "Turing Hall 101"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id2,
            subjectName = "Applied Calculus",
            subjectColorHex = "#8B5CF6",
            title = "MATH 201: Eigenvalues Seminar",
            dayOfWeek = 1,
            startTime = "11:00",
            endTime = "12:30",
            studyType = "Lecture",
            location = "Math Annex 204"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id3,
            subjectName = "Machine Learning",
            subjectColorHex = "#10B981",
            title = "AI 302: Deep Focus Self Study",
            dayOfWeek = 1,
            startTime = "14:30",
            endTime = "16:00",
            studyType = "Self-Study",
            location = "Campus Library 3F"
        ))

        // Tuesday (2)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id4,
            subjectName = "Computer Systems",
            subjectColorHex = "#F59E0B",
            title = "CS 230: Cache Memory Architecture",
            dayOfWeek = 2,
            startTime = "10:00",
            endTime = "11:30",
            studyType = "Lecture",
            location = "Hardware Lab 12"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id1,
            subjectName = "Data Structures & Algorithms",
            subjectColorHex = "#38BDF8",
            title = "CS 210: Coding Lab & LeetCode Practice",
            dayOfWeek = 2,
            startTime = "14:00",
            endTime = "16:00",
            studyType = "Lab",
            location = "CS Lab 4B"
        ))

        // Wednesday (3)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id2,
            subjectName = "Applied Calculus",
            subjectColorHex = "#8B5CF6",
            title = "MATH 201: Multivariable Derivatives",
            dayOfWeek = 3,
            startTime = "09:00",
            endTime = "10:30",
            studyType = "Lecture",
            location = "Math Annex 204"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id3,
            subjectName = "Machine Learning",
            subjectColorHex = "#10B981",
            title = "AI 302: Neural Net Backprop Workshop",
            dayOfWeek = 3,
            startTime = "11:00",
            endTime = "13:00",
            studyType = "Lecture",
            location = "Tech Auditorium"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id1,
            subjectName = "Data Structures",
            subjectColorHex = "#38BDF8",
            title = "CS 210: Midterm Prep & Review",
            dayOfWeek = 3,
            startTime = "16:00",
            endTime = "17:30",
            studyType = "Revision",
            location = "Library Quiet Zone"
        ))

        // Thursday (4)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id4,
            subjectName = "Computer Systems",
            subjectColorHex = "#F59E0B",
            title = "CS 230: Assembly Programming Lab",
            dayOfWeek = 4,
            startTime = "10:30",
            endTime = "12:30",
            studyType = "Lab",
            location = "Hardware Lab 12"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id2,
            subjectName = "Applied Calculus",
            subjectColorHex = "#8B5CF6",
            title = "MATH 201: Problem Set 4 Group Study",
            dayOfWeek = 4,
            startTime = "15:00",
            endTime = "17:00",
            studyType = "Self-Study",
            location = "Student Center 202"
        ))

        // Friday (5)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id1,
            subjectName = "Data Structures",
            subjectColorHex = "#38BDF8",
            title = "CS 210: Graph Algorithms Lecture",
            dayOfWeek = 5,
            startTime = "09:30",
            endTime = "11:00",
            studyType = "Lecture",
            location = "Turing Hall 101"
        ))
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id3,
            subjectName = "Machine Learning",
            subjectColorHex = "#10B981",
            title = "AI 302: PyTorch Project Lab",
            dayOfWeek = 5,
            startTime = "13:30",
            endTime = "15:30",
            studyType = "Lab",
            location = "Tech Lab Alpha"
        ))

        // Saturday (6)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id3,
            subjectName = "Machine Learning",
            subjectColorHex = "#10B981",
            title = "Weekend Deep Work: Kaggle Competition",
            dayOfWeek = 6,
            startTime = "10:00",
            endTime = "12:00",
            studyType = "Self-Study",
            location = "Home Study Desk"
        ))

        // Sunday (7)
        dao.insertScheduleItem(ScheduleItemEntity(
            subjectId = id2,
            subjectName = "Applied Calculus",
            subjectColorHex = "#8B5CF6",
            title = "Weekly Review & Week Ahead Schedule Plan",
            dayOfWeek = 7,
            startTime = "16:00",
            endTime = "17:30",
            studyType = "Revision",
            location = "Home Study Desk"
        ))

        // 3. Study Tasks / Assignments
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        dao.insertTask(StudyTaskEntity(
            subjectId = id1,
            subjectName = "Data Structures & Algorithms",
            title = "Implement AVL Tree Self-Balancing Rotations",
            description = "Complete the insertion and deletion balancing cases. Ensure test suite passes.",
            dueDate = now + oneDay,
            priority = "High",
            isCompleted = false,
            estimatedMinutes = 90,
            subtasks = "Write left-rotation helper\nWrite right-rotation helper\nHandle double rotations (RL and LR)\nRun test cases with 10k random elements"
        ))
        dao.insertTask(StudyTaskEntity(
            subjectId = id3,
            subjectName = "Machine Learning Foundations",
            title = "Train Convolutional Filter on MNIST",
            description = "Tune learning rate with Adam optimizer, report confusion matrix and accuracy curve.",
            dueDate = now + oneDay * 3,
            priority = "Medium",
            isCompleted = false,
            estimatedMinutes = 60,
            subtasks = "Normalize dataset\nImplement 2-layer CNN\nPlot training vs validation loss curve"
        ))
        dao.insertTask(StudyTaskEntity(
            subjectId = id2,
            subjectName = "Applied Calculus",
            title = "Solve Multivariable Chain Rule Problem Set",
            description = "Problems 14 through 28 in Chapter 14.5.",
            dueDate = now + oneDay * 4,
            priority = "Medium",
            isCompleted = false,
            estimatedMinutes = 75,
            subtasks = "Review partial derivative rules\nComplete odd problems 15-27\nVerify solutions with textbook"
        ))
        dao.insertTask(StudyTaskEntity(
            subjectId = id4,
            subjectName = "Computer Systems",
            title = "Complete Cache Hit/Miss Simulation Analysis",
            description = "Compare direct-mapped vs 4-way set associative hit ratios.",
            dueDate = now - oneDay, // Overdue/recent
            priority = "High",
            isCompleted = true,
            estimatedMinutes = 45,
            subtasks = "Generate memory trace\nRun cache simulator\nWrite 1-page reflection"
        ))

        // 4. Sample Past Study Sessions (for live charts)
        dao.insertSession(StudySessionEntity(
            subjectName = "Data Structures & Algorithms",
            durationMinutes = 50,
            timestamp = now - oneDay * 3,
            studyMode = "Deep Focus",
            notes = "Solved 3 BST leetcode medium problems. Good flow state.",
            productivityRating = 5
        ))
        dao.insertSession(StudySessionEntity(
            subjectName = "Applied Calculus",
            durationMinutes = 45,
            timestamp = now - oneDay * 2,
            studyMode = "Pomodoro",
            notes = "Reviewed matrix diagonalization formulas.",
            productivityRating = 4
        ))
        dao.insertSession(StudySessionEntity(
            subjectName = "Machine Learning Foundations",
            durationMinutes = 60,
            timestamp = now - oneDay * 1,
            studyMode = "Deep Focus",
            notes = "Understood gradient descent backprop calculus derivation.",
            productivityRating = 5
        ))
        dao.insertSession(StudySessionEntity(
            subjectName = "Data Structures & Algorithms",
            durationMinutes = 30,
            timestamp = now - 3600000L * 4,
            studyMode = "Pomodoro",
            notes = "Quick review before lecture.",
            productivityRating = 4
        ))

        // 5. Exam Countdowns
        dao.insertExam(ExamCountdownEntity(
            subjectName = "Data Structures & Algorithms",
            title = "Midterm Exam: Trees, Graphs & Sorting",
            targetDate = now + oneDay * 5,
            weightPercent = 30,
            preparednessScore = 75
        ))
        dao.insertExam(ExamCountdownEntity(
            subjectName = "Machine Learning Foundations",
            title = "Midterm Project Presentation",
            targetDate = now + oneDay * 12,
            weightPercent = 25,
            preparednessScore = 60
        ))
        dao.insertExam(ExamCountdownEntity(
            subjectName = "Applied Calculus & Linear Algebra",
            title = "Final Examination",
            targetDate = now + oneDay * 24,
            weightPercent = 40,
            preparednessScore = 55
        ))
    }
}
