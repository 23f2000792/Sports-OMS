package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("Sports Operations", appName)
  }

  @Test
  fun `verify logic and repository initial state`() {
    val repo = com.example.data.SportsOpsRepositoryImpl()
    val initialTasks = repo.tasks.value
    org.junit.Assert.assertTrue(initialTasks.isNotEmpty())
    val initialEvents = repo.events.value
    org.junit.Assert.assertTrue(initialEvents.isNotEmpty())
  }
}
