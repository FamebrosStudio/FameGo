package com.example

import com.example.ui.components.WheelSection
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testWheelSectionTabMappings() {
    assertEquals(WheelSection.DASHBOARD, WheelSection.fromTab("home"))
    assertEquals(WheelSection.DASHBOARD, WheelSection.fromTab("dashboard"))
    assertEquals(WheelSection.BOOKINGS, WheelSection.fromTab("bookings"))
    assertEquals(WheelSection.BOOK_SHOOT, WheelSection.fromTab("book"))
    assertEquals(WheelSection.ALERTS, WheelSection.fromTab("notifications"))
    assertEquals(WheelSection.ALERTS, WheelSection.fromTab("alerts"))
    assertEquals(WheelSection.PROFILE, WheelSection.fromTab("profile"))
  }

  @Test
  fun testWheelSectionIndicesAndLabels() {
    assertEquals(0, WheelSection.DASHBOARD.index)
    assertEquals(1, WheelSection.BOOKINGS.index)
    assertEquals(2, WheelSection.BOOK_SHOOT.index)
    assertEquals(3, WheelSection.ALERTS.index)
    assertEquals(4, WheelSection.PROFILE.index)

    assertEquals("dashboard", WheelSection.DASHBOARD.label)
    assertEquals("bookings", WheelSection.BOOKINGS.label)
    assertEquals("✦ book shoot", WheelSection.BOOK_SHOOT.label)
    assertEquals("alerts", WheelSection.ALERTS.label)
    assertEquals("profile", WheelSection.PROFILE.label)

    assertTrue(WheelSection.BOOK_SHOOT.isQuickAction)
    assertFalse(WheelSection.DASHBOARD.isQuickAction)
  }
}
