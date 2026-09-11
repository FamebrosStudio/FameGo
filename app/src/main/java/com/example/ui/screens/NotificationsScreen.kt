package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SoftCard
import com.example.ui.theme.fameGoRise
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite

data class ProductionNotification(
  val id: String,
  val title: String,
  val body: String,
  val time: String,
  val isImportant: Boolean,
  val group: String, // NOW, TODAY, EARLIER
  val bookingId: String? = null
)

@Composable
fun NotificationsScreen(
  onOpenBooking: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val notifications = listOf(
    ProductionNotification(
      id = "n1",
      title = "Crew confirmed for Friday",
      body = "Aarav is confirmed for your shoot at Veranda Studio.",
      time = "Just now",
      isImportant = true,
      group = "New",
      bookingId = "booking_1"
    ),
    ProductionNotification(
      id = "n2",
      title = "Aarav is on the way",
      body = "Estimated arrival in 15 minutes.",
      time = "10:30 AM",
      isImportant = true,
      group = "Today",
      bookingId = "booking_1"
    ),
    ProductionNotification(
      id = "n3",
      title = "Shoot started",
      body = "Camera rolling at Veranda Studio.",
      time = "9:00 AM",
      isImportant = false,
      group = "Today",
      bookingId = "booking_1"
    ),
    ProductionNotification(
      id = "n4",
      title = "Your footage is ready to download",
      body = "Raw files and selects are ready in your folder.",
      time = "Yesterday",
      isImportant = false,
      group = "Earlier",
      bookingId = "booking_1"
    )
  )

  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp)
        .fameGoRise()
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Notifications",
        color = FameGoWhite,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      )
      Text(
        text = "Updates about your shoots",
        color = FameGoTextMuted,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
      )

      if (notifications.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "All caught up",
              color = FameGoWhite,
              fontSize = 17.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "You will see shoot updates here.",
              color = FameGoTextMuted,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      } else {
        val groups = listOf("New", "Today", "Earlier")
        groups.forEach { grp ->
          val itemsInGroup = notifications.filter { it.group == grp }
          if (itemsInGroup.isNotEmpty()) {
            Text(
              text = grp,
              color = FameGoTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              itemsInGroup.forEach { item ->
                SoftCard(
                  onClick = { item.bookingId?.let { onOpenBooking(it) } },
                  testTag = "notification_${item.id}"
                ) {
                  Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                  ) {
                    Box(
                      modifier = Modifier
                        .padding(top = 5.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (item.isImportant) FameGoGold else FameGoTextMuted.copy(alpha = 0.4f))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = item.title,
                          color = FameGoWhite,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.SemiBold
                        )
                        Text(
                          text = item.time,
                          color = FameGoTextMuted,
                          fontSize = 11.sp
                        )
                      }

                      Text(
                        text = item.body,
                        color = FameGoTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 3.dp)
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(110.dp))
    }
  }
}
