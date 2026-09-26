package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.ui.viewmodel.TimerMode
import com.example.ui.viewmodel.TimerState
import java.util.Locale

/**
 * Reusable Focus Timer Component with Start, Pause, Reset, Quick Time Adjustment,
 * Mode Switching, and Subject Selection functionality.
 */
@Composable
fun FocusTimerComponent(
    timerMode: TimerMode,
    timerState: TimerState,
    isBreak: Boolean,
    remainingSeconds: Int,
    totalSeconds: Int,
    stopwatchSeconds: Int,
    currentSubject: String,
    availableSubjects: List<SubjectEntity>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLogSession: () -> Unit,
    onModeChange: (TimerMode) -> Unit,
    onSubjectChange: (String) -> Unit,
    onAdjustTime: (deltaSeconds: Int) -> Unit,
    modifier: Modifier = Modifier,
    dialSize: Dp = 230.dp,
    showModeSelector: Boolean = true,
    showSubjectSelector: Boolean = true
) {
    val progress = if (timerMode == TimerMode.STOPWATCH) {
        1.0f
    } else if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else {
        0.0f
    }

    val displayMinutes = if (timerMode == TimerMode.STOPWATCH) {
        stopwatchSeconds / 60
    } else {
        remainingSeconds / 60
    }

    val displaySecs = if (timerMode == TimerMode.STOPWATCH) {
        stopwatchSeconds % 60
    } else {
        remainingSeconds % 60
    }

    val timeString = String.format(Locale.getDefault(), "%02d:%02d", displayMinutes, displaySecs)

    // Pulsing animation for dial when running
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (timerState == TimerState.RUNNING) 1.03f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_timer_component"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Mode Switcher
        if (showModeSelector) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TimerMode.values()) { mode ->
                    val isSelected = mode == timerMode
                    Surface(
                        modifier = Modifier
                            .testTag("timer_mode_${mode.name}")
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = timerState != TimerState.RUNNING) {
                                onModeChange(mode)
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = mode.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 2. Subject Selector
        if (showSubjectSelector && availableSubjects.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Current Course Focus",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSubjects) { sub ->
                        val isSelected = sub.name == currentSubject
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSubjectChange(sub.name) },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = sub.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Timer Dial Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phase Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBreak) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isBreak) "☕ RECOVERY BREAK" else "🎯 DEEP FOCUS SESSION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBreak) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Circular Timer Meter with Pulsing scale
                Box(
                    modifier = Modifier.scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressMeter(
                        progress = progress,
                        size = dialSize,
                        strokeWidth = 14.dp,
                        primaryColor = if (isBreak) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeString,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentSubject,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Interval Adjuster (+5m / -5m)
                if (timerMode != TimerMode.STOPWATCH) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onAdjustTime(-300) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("adjust_time_minus_5")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-5m", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("5m", fontSize = 11.sp)
                        }

                        Text(
                            text = when (timerState) {
                                TimerState.RUNNING -> "In Session"
                                TimerState.PAUSED -> "Paused"
                                TimerState.COMPLETED -> "Finished!"
                                else -> "Ready"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { onAdjustTime(300) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("adjust_time_plus_5")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+5m", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("5m", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Core Action Controls: Reset, Start/Pause, Log
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("reset_timer_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Main Start / Pause Button
                    if (timerState == TimerState.RUNNING) {
                        Button(
                            onClick = onPause,
                            modifier = Modifier
                                .size(76.dp)
                                .testTag("pause_timer_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pause Timer",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .size(76.dp)
                                .testTag("start_timer_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Start Timer",
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Log / Finish Session Button
                    IconButton(
                        onClick = onLogSession,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("log_session_button")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Log Completed Session",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
