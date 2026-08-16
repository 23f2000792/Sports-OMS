package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Priority
import com.example.model.TaskItem
import com.example.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("Sports Operations", appName)
  }

  @Test
  fun `verify repository task creation and state management`() {
    val repo = com.example.data.SportsOpsRepositoryImpl()
    val newTask = TaskItem(
      id = "TEST-TASK-001",
      title = "Real Turf Inspection",
      vertical = "Logistics",
      teamMemberId = "TM-01",
      teamMemberName = "Arjun Patel",
      taskType = "Infrastructure",
      priority = Priority.HIGH,
      assignedById = "TM-01",
      assignedByName = "Arjun Patel",
      dateAssigned = "2026-08-16",
      deadline = "2026-08-25"
    )
    repo.createOrUpdateTask(newTask)
    val tasks = repo.tasks.value
    assertTrue(tasks.any { it.id == "TEST-TASK-001" && it.title == "Real Turf Inspection" })

    repo.updateTaskStatus("TEST-TASK-001", TaskStatus.IN_PROGRESS, progress = 50)
    val updated = repo.tasks.value.find { it.id == "TEST-TASK-001" }
    assertEquals(TaskStatus.IN_PROGRESS, updated?.status)
    assertEquals(50, updated?.progressPercent)
  }
}
