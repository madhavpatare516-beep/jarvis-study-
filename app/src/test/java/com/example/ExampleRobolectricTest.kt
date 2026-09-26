package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.TimerMode
import com.example.ui.viewmodel.TimerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jarvis Study", appName)
  }

  @Test
  fun `test focus timer start pause reset transitions`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = StudyViewModel(app)

    // Initial state
    assertEquals(TimerState.IDLE, viewModel.timerState.value)
    assertEquals(TimerMode.POMODORO.workMinutes * 60, viewModel.remainingSeconds.value)

    // Start
    viewModel.startTimer()
    assertEquals(TimerState.RUNNING, viewModel.timerState.value)

    // Pause
    viewModel.pauseTimer()
    assertEquals(TimerState.PAUSED, viewModel.timerState.value)

    // Reset
    viewModel.resetTimer()
    assertEquals(TimerState.IDLE, viewModel.timerState.value)
    assertEquals(TimerMode.POMODORO.workMinutes * 60, viewModel.remainingSeconds.value)

    // Mode change to Deep Focus (50m = 3000s)
    viewModel.setTimerMode(TimerMode.DEEP_FOCUS)
    assertEquals(TimerMode.DEEP_FOCUS, viewModel.timerMode.value)
    assertEquals(50 * 60, viewModel.remainingSeconds.value)

    // Adjust time +5 minutes (300s)
    viewModel.adjustTimerSeconds(300)
    assertEquals(55 * 60, viewModel.remainingSeconds.value)
  }
}
