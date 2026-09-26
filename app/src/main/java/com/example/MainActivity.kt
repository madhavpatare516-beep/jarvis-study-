package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.IconButton
import com.example.ui.components.PsychologyAndPrivacyDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.JarvisStudyTheme
import com.example.ui.viewmodel.StudyViewModel

enum class NavDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", "Today", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SCHEDULE("schedule", "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    FOCUS("focus", "Focus", Icons.Filled.Timer, Icons.Outlined.Timer),
    TASKS("tasks", "Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    SUBJECTS("subjects", "Courses", Icons.Filled.School, Icons.Outlined.School),
    ANALYTICS("analytics", "Analytics", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val psychologyTheme by viewModel.psychologyTheme.collectAsStateWithLifecycle()
            var showPsychologyDialog by remember { mutableStateOf(false) }

            JarvisStudyTheme(psychologyTheme = psychologyTheme) {
                var currentDestination by remember { mutableStateOf(NavDestination.DASHBOARD) }
                val streakDays by viewModel.currentStreakDays.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_app_icon),
                                        contentDescription = "Jarvis Study Logo",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Jarvis Study",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showPsychologyDialog = true },
                                    modifier = Modifier.testTag("psychology_colors_button")
                                ) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = "Psychology Colors & Privacy Vault",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    modifier = Modifier.padding(end = 12.dp),
                                    shape = CircleShape,
                                    color = FlameOrange.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak",
                                            tint = AmberStreak,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$streakDays",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AmberStreak
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 6.dp
                        ) {
                            NavDestination.values().forEach { destination ->
                                val isSelected = currentDestination == destination
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = destination.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = destination.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_${destination.route}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { destination ->
                            when (destination) {
                                NavDestination.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToTimer = { subject ->
                                        viewModel.setTimerSubject(subject)
                                        currentDestination = NavDestination.FOCUS
                                    },
                                    onNavigateToSchedule = { currentDestination = NavDestination.SCHEDULE },
                                    onNavigateToTasks = { currentDestination = NavDestination.TASKS },
                                    onNavigateToAi = { currentDestination = NavDestination.ANALYTICS }
                                )
                                NavDestination.SCHEDULE -> ScheduleScreen(
                                    viewModel = viewModel,
                                    onStartFocusSession = { subject ->
                                        viewModel.setTimerSubject(subject)
                                        currentDestination = NavDestination.FOCUS
                                    }
                                )
                                NavDestination.FOCUS -> FocusTimerScreen(
                                    viewModel = viewModel
                                )
                                NavDestination.TASKS -> TasksScreen(
                                    viewModel = viewModel
                                )
                                NavDestination.SUBJECTS -> SubjectsScreen(
                                    viewModel = viewModel
                                )
                                NavDestination.ANALYTICS -> AnalyticsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                if (showPsychologyDialog) {
                    PsychologyAndPrivacyDialog(
                        viewModel = viewModel,
                        onDismiss = { showPsychologyDialog = false }
                    )
                }
            }
        }
    }
}
