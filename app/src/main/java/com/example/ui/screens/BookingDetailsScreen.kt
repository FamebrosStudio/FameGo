package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.ShootCategory
import com.example.ui.components.FlowPill
import com.example.ui.components.FlowPillState
import com.example.ui.components.LiveOrb
import com.example.ui.components.SoftCard
import com.example.ui.theme.fameGoRise
import com.example.ui.theme.FameGoAccentCyan
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextPrimary
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite

@Composable
fun BookingDetailsScreen(
  bookingId: String,
  onBack: () -> Unit,
  onOpenChat: (String) -> Unit,
  onRebook: (ShootCategory) -> Unit,
  onBookSameCrew: (Booking) -> Unit,
  onContactSupport: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by FameGoRepository.bookings.collectAsState()
  val favoriteIds by FameGoRepository.favoriteCrewIds.collectAsState()
  val ratings by FameGoRepository.ratings.collectAsState()
  val liveSharing by FameGoRepository.liveSharing.collectAsState()
  val livePoints by FameGoRepository.livePoints.collectAsState()
  // A stale deep link must not silently display a different booking.
  val booking = bookings.firstOrNull { it.id == bookingId }

  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FameGoBg)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp)
        .fameGoRise()
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = FameGoTextSecondary
          )
        }
        Text(
          text = "Shoot details",
          color = FameGoWhite,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold
        )
        // Chat shortcut
        IconButton(
          onClick = { booking?.id?.let(onOpenChat) },
          enabled = booking != null,
          modifier = Modifier.testTag("call_sheet_chat_icon")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Message",
            tint = FameGoGold
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      if (booking == null) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Booking not found", color = FameGoWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
              text = "It may have been cancelled or removed.",
              color = FameGoTextMuted, fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }

      if (booking != null) {
        // Main Live Shoot Card
        SoftCard(
          shape = RoundedCornerShape(22.dp),
          isElevated = true,
          testTag = "call_sheet_main_card"
        ) {
          Column(modifier = Modifier.padding(22.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                LiveOrb(
                  color = when (booking.status) {
                    BookingStatus.IN_PROGRESS -> FameGoGold
                    BookingStatus.CONFIRMED -> FameGoSuccessGreen
                    BookingStatus.PENDING -> FameGoAccentCyan
                    else -> FameGoTextMuted
                  },
                  size = 8.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = when (booking.status) {
                    BookingStatus.IN_PROGRESS -> "Shoot in progress"
                    BookingStatus.CONFIRMED -> "Crew confirmed"
                    BookingStatus.COMPLETED -> "Wrap completed"
                    else -> "Finding crew"
                  },
                  color = when (booking.status) {
                    BookingStatus.IN_PROGRESS -> FameGoGold
                    BookingStatus.CONFIRMED -> FameGoSuccessGreen
                    else -> FameGoTextSecondary
                  },
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Text(
                text = "₹${booking.estimatedBudget}",
                color = FameGoWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = booking.title,
              color = FameGoWhite,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = "${booking.date} • Call time: ${booking.time} (${booking.durationHours} hours)",
              color = FameGoTextSecondary,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Venue
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = FameGoGold,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${booking.location.venueName}, ${booking.location.address}",
                color = FameGoTextPrimary,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Assigned Crew Roster
        if (booking.assignedCrew.isNotEmpty()) {
          Text(
            text = "Assigned crew",
            color = FameGoTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            booking.assignedCrew.forEach { crew ->
              SoftCard(
                onClick = { onOpenChat(booking.id) },
                testTag = "assigned_crew_item_${crew.crewId}"
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(FameGoCardElevated)
                      .border(1.dp, FameGoGold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = crew.crewName.take(1),
                      color = FameGoGold,
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Spacer(modifier = Modifier.width(14.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = crew.crewName,
                        color = FameGoWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "${crew.rating} ★",
                        color = FameGoGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                    Text(
                      text = "${crew.role.title} • Verified kit",
                      color = FameGoTextSecondary,
                      fontSize = 12.sp,
                      modifier = Modifier.padding(top = 1.dp)
                    )
                    Text(
                      text = crew.gearList.joinToString(" • "),
                      color = FameGoTextMuted,
                      fontSize = 11.sp,
                      modifier = Modifier.padding(top = 2.dp)
                    )
                    if (crew.phone.isNotBlank()) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Phone,
                          contentDescription = null,
                          tint = FameGoTextMuted,
                          modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = crew.phone,
                          color = FameGoTextSecondary,
                          fontSize = 12.sp,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FameGoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle),
                    modifier = Modifier.clickable { onOpenChat(booking.id) }
                  ) {
                    Text(
                      text = "Message",
                      color = FameGoGold,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }

            // Favorite toggle for the primary crew member
            val primaryCrew = booking.assignedCrew.firstOrNull()
            if (primaryCrew != null) {
              val isFavorite = primaryCrew.crewId in favoriteIds
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { FameGoRepository.toggleFavoriteCrew(primaryCrew.crewId) }
                  .padding(vertical = 4.dp)
                  .testTag("favorite_crew_${primaryCrew.crewId}"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (isFavorite) FameGoGold else FameGoTextMuted,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isFavorite) "Saved to favorite crew" else "Add to favorite crew",
                  color = if (isFavorite) FameGoGold else FameGoTextSecondary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }

        // Live crew location (visible once crew is assigned and shoot is active)
        if (booking.assignedCrew.isNotEmpty() &&
          (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.IN_PROGRESS)
        ) {
          val sharing = booking.id in liveSharing
          Text(
            text = "Live location",
            color = FameGoTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))
          LiveLocationMap(
            venueName = booking.location.venueName,
            sharing = sharing,
            latitude = livePoints[booking.id]?.latitude,
            longitude = livePoints[booking.id]?.longitude,
            statusLabel = livePoints[booking.id]?.label
              ?: if (sharing) "Locating crew…" else "Waiting for crew to share location"
          )
          Spacer(modifier = Modifier.height(20.dp))
        }

        // Brief & Scope
        Text(
          text = "Shoot brief",
          color = FameGoTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SoftCard {
          Text(
            text = booking.brief.ifEmpty { "Standard production coverage as requested." },
            color = FameGoTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(16.dp)
          )
        }

        // Rating + book-same-crew (completed shoots with assigned crew)
        if (booking.status == BookingStatus.COMPLETED && booking.assignedCrew.isNotEmpty()) {
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = "Rate your crew",
            color = FameGoTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))
          val rateable = booking.assignedCrew.first()
          val existingRating = ratings["${booking.id}:${rateable.crewId}"]
          SoftCard(
            isElevated = true,
            testTag = "rate_crew_card"
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = rateable.crewName,
                color = FameGoWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (existingRating != null) {
                Spacer(modifier = Modifier.height(8.dp))
                RatingStars(stars = existingRating.stars, onSelect = {}, enabled = false)
                if (existingRating.review.isNotBlank()) {
                  Text(
                    text = "\"${existingRating.review}\"",
                    color = FameGoTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                  )
                }
              } else {
                var stars by remember(booking.id) { mutableIntStateOf(0) }
                var review by remember(booking.id) { mutableStateOf("") }
                Spacer(modifier = Modifier.height(8.dp))
                RatingStars(stars = stars, onSelect = { stars = it }, enabled = true)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                  value = review,
                  onValueChange = { review = it },
                  placeholder = { Text("Add a note for the crew (optional)", color = FameGoTextMuted, fontSize = 12.sp) },
                  singleLine = true,
                  shape = RoundedCornerShape(12.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = FameGoWhite,
                    unfocusedTextColor = FameGoWhite,
                    focusedContainerColor = FameGoCardElevated,
                    unfocusedContainerColor = FameGoCardElevated,
                    focusedBorderColor = FameGoGold,
                    unfocusedBorderColor = FameGoBorderSubtle
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rate_crew_review")
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowPill(
                  state = FlowPillState.CUSTOM,
                  customText = "Submit rating",
                  enabled = stars > 0,
                  onClick = {
                    FameGoRepository.submitRating(booking.id, rateable.crewId, stars, review)
                  },
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "rate_crew_submit"
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          FlowPill(
            state = FlowPillState.CUSTOM,
            customText = "Book same crew again",
            onClick = {
              FameGoRepository.prepareRebooking(booking.id)?.let(onBookSameCrew)
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "book_same_crew_button"
          )
        }
      }

      Spacer(modifier = Modifier.height(110.dp))
    }

    // Fixed Bottom Action Flow Pill
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 24.dp)
    ) {
      if (booking?.status == BookingStatus.COMPLETED) {
        FlowPill(
          state = FlowPillState.CUSTOM,
          customText = "Book again",
          onClick = { onRebook(booking.category) },
          testTag = "call_sheet_rebook_pill"
        )
      } else {
        FlowPill(
          state = FlowPillState.CUSTOM,
          customText = "Message crew",
          customIcon = Icons.AutoMirrored.Filled.Chat,
          onClick = { booking?.id?.let(onOpenChat) },
          testTag = "call_sheet_chat_pill"
        )
      }
    }
  }
}

@Composable
private fun RatingStars(
  stars: Int,
  onSelect: (Int) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.testTag("rate_crew_stars"),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    (1..5).forEach { value ->
      IconButton(
        onClick = { onSelect(value) },
        enabled = enabled,
        modifier = Modifier
          .size(40.dp)
          .testTag("rate_star_$value")
      ) {
        Icon(
          imageVector = if (value <= stars) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = "$value star",
          tint = if (value <= stars) FameGoGold else FameGoTextMuted,
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

@Composable
private fun LiveLocationMap(
  venueName: String,
  sharing: Boolean,
  latitude: Double?,
  longitude: Double?,
  statusLabel: String,
  modifier: Modifier = Modifier
) {
  SoftCard(
    shape = RoundedCornerShape(18.dp),
    isElevated = true,
    modifier = modifier.fillMaxWidth(),
    testTag = "live_location_map"
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
          .background(Color(0xFF101216)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          // Street grid
          for (i in 1..6) {
            val y = h * i / 7f
            drawLine(
              color = Color(0xFFF5B942).copy(alpha = 0.08f),
              start = Offset(0f, y),
              end = Offset(w, y),
              strokeWidth = 1.5f
            )
          }
          for (i in 1..4) {
            val x = w * i / 5f
            drawLine(
              color = Color(0xFFF5B942).copy(alpha = 0.08f),
              start = Offset(x, 0f),
              end = Offset(x, h),
              strokeWidth = 1.5f
            )
          }
          // Main roads
          drawLine(
            color = Color(0xFFF5B942).copy(alpha = 0.22f),
            start = Offset(0f, h * 0.62f),
            end = Offset(w, h * 0.42f),
            strokeWidth = 5f
          )
          drawLine(
            color = Color(0xFFF5B942).copy(alpha = 0.22f),
            start = Offset(w * 0.68f, 0f),
            end = Offset(w * 0.42f, h),
            strokeWidth = 5f
          )
          // Venue pin
          drawCircle(color = FameGoGold, radius = 7f, center = Offset(w * 0.5f, h * 0.52f))
          drawCircle(
            color = FameGoGold.copy(alpha = 0.35f),
            radius = 16f,
            center = Offset(w * 0.5f, h * 0.52f),
            style = Stroke(width = 3f)
          )
          if (sharing && latitude != null && longitude != null) {
            // Crew dot drifts around the venue in demo mode
            val dx = ((longitude - 72.8295) * 22000f).toFloat().coerceIn(-w * 0.32f, w * 0.32f)
            val dy = ((19.0596 - latitude) * 22000f).toFloat().coerceIn(-h * 0.3f, h * 0.3f)
            val crewAt = Offset(w * 0.5f + dx, h * 0.52f + dy)
            drawCircle(color = FameGoSuccessGreen.copy(alpha = 0.3f), radius = 22f, center = crewAt)
            drawCircle(color = FameGoSuccessGreen, radius = 9f, center = crewAt)
            drawCircle(color = Color.White, radius = 3.5f, center = crewAt)
          }
        }
        if (!sharing) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xCC141518),
            border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorderSubtle)
          ) {
            Text(
              text = "Location sharing off",
              color = FameGoTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
          }
        }
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        LiveOrb(
          color = if (sharing) FameGoSuccessGreen else FameGoTextMuted,
          size = 7.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = venueName.ifBlank { "Shoot venue" },
            color = FameGoWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (sharing && latitude != null && longitude != null) {
              "$statusLabel • ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}"
            } else {
              statusLabel
            },
            color = FameGoTextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}
