package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.example.ui.components.FameGoWheelNavigation
import com.example.ui.theme.FameGoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WheelNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setWheelContent(
    currentTab: String = "home",
    onNavigate: (String) -> Unit = {},
    onOpenBookingFlow: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      FameGoTheme {
        FameGoWheelNavigation(
          currentTab = currentTab,
          onNavigate = onNavigate,
          onOpenBookingFlow = onOpenBookingFlow
        )
      }
    }
  }

  @Test
  fun `wheel renders all five sections`() {
    // Centered on "book" so every label falls inside the visible arc.
    setWheelContent(currentTab = "book")
    composeTestRule.onNodeWithTag("famego_navigation_wheel").assertExists()
    composeTestRule.onNodeWithText("dashboard").assertExists()
    composeTestRule.onNodeWithText("bookings").assertExists()
    composeTestRule.onNodeWithText("✦ book shoot").assertExists()
    composeTestRule.onNodeWithText("alerts").assertExists()
    composeTestRule.onNodeWithText("profile").assertExists()
  }

  @Test
  fun `swipe left on wheel advances to next tab`() {
    var navigated: String? = null
    setWheelContent(currentTab = "home", onNavigate = { navigated = it })
    // Short, calm drag: ~1 snap section (a full fling carries momentum further).
    composeTestRule.onNodeWithTag("famego_navigation_wheel").performTouchInput {
      swipe(
        start = center + androidx.compose.ui.geometry.Offset(80f, 0f),
        end = center - androidx.compose.ui.geometry.Offset(80f, 0f),
        durationMillis = 600
      )
    }
    composeTestRule.waitForIdle()
    assertEquals("bookings", navigated)
  }

  @Test
  fun `swipe right on wheel goes back to previous tab`() {
    var navigated: String? = null
    setWheelContent(currentTab = "bookings", onNavigate = { navigated = it })
    composeTestRule.onNodeWithTag("famego_navigation_wheel").performTouchInput {
      swipe(
        start = center - androidx.compose.ui.geometry.Offset(80f, 0f),
        end = center + androidx.compose.ui.geometry.Offset(80f, 0f),
        durationMillis = 600
      )
    }
    composeTestRule.waitForIdle()
    assertEquals("home", navigated)
  }

  @Test
  fun `tapping book shoot dot navigates to book tab`() {
    var navigated: String? = null
    setWheelContent(currentTab = "home", onNavigate = { navigated = it })
    composeTestRule.onNodeWithTag("wheel_dot_2").performClick()
    composeTestRule.waitForIdle()
    assertEquals("book", navigated)
  }
}
