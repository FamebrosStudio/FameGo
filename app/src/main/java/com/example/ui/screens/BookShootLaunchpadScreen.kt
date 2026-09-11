package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShootCategory
import com.example.ui.components.CharacterState
import com.example.ui.components.FameGoCharacterIllustration
import com.example.ui.components.SoftCard
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoGoldContainer
import com.example.ui.theme.FameGoSuccessGreen
import com.example.ui.theme.FameGoSurface
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import com.example.ui.theme.fameGoBreathe
import com.example.ui.theme.fameGoRise

/**
 * Launchpad screen displayed when "✦ book shoot" is active on the rotating wheel.
 * Allows clients to browse production categories, inspect instant day rates,
 * and immediately enter the comprehensive 6-step booking flow.
 */
@Composable
fun BookShootLaunchpadScreen(
  onStartBooking: (ShootCategory?) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  var selectedCategory by remember { mutableStateOf<ShootCategory?>(ShootCategory.VIDEO) }

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

      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Book a Shoot",
            color = FameGoWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
          )
          Text(
            text = "Verified cinematographers, sound & rigs in 45m",
            color = FameGoTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FameGoGoldContainer)
            .border(1.dp, FameGoGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(FameGoSuccessGreen)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Live Dispatch",
              color = FameGoGold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Hero Character & Quick Pitch
      SoftCard(
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FameGoCharacterIllustration(
            state = CharacterState.FINDING_CREW,
            size = 72.dp,
            modifier = Modifier.fameGoBreathe()
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Studio-Grade On Demand",
              color = FameGoWhite,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Sony FX6 / FX3 kits, prime lenses, and insured crew ready across Mumbai & Delhi.",
              color = FameGoTextSecondary,
              fontSize = 12.sp,
              lineHeight = 17.sp,
              modifier = Modifier.padding(top = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Select Production Type",
        color = FameGoTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Category Cards Grid
      val categories = listOf(
        CategoryLaunchpadItem(
          category = ShootCategory.VIDEO,
          title = "Commercial / Ad Video",
          price = "₹28,000 / day",
          kit = "Sony FX6 • Prime Pack • Gaffer",
          icon = Icons.Default.Movie
        ),
        CategoryLaunchpadItem(
          category = ShootCategory.FASHION,
          title = "Fashion & Editorial",
          price = "₹24,000 / day",
          kit = "Sony FX3 • Prime Kit • Stylist Assist",
          icon = Icons.Default.Videocam
        ),
        CategoryLaunchpadItem(
          category = ShootCategory.PRODUCT,
          title = "Product & Commercial",
          price = "₹20,000 / day",
          kit = "Macro Cinema Rig • Turntable • Softbox",
          icon = Icons.Default.CameraAlt
        ),
        CategoryLaunchpadItem(
          category = ShootCategory.EVENT,
          title = "Event & Live Stream",
          price = "₹18,000 / day",
          kit = "Multi-Cam Setup • Wireless HDMI",
          icon = Icons.Default.Schedule
        ),
        CategoryLaunchpadItem(
          category = ShootCategory.CORPORATE,
          title = "Corporate & Interview",
          price = "₹16,000 / day",
          kit = "4K Cinema • Wireless Mic • Key Light",
          icon = Icons.Default.FlashOn
        )
      )

      categories.forEach { item ->
        val isSelected = selectedCategory == item.category
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (isSelected) FameGoCardElevated else FameGoCard,
          border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) FameGoGold else FameGoBorderSubtle
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable {
              selectedCategory = item.category
              onStartBooking(item.category)
            }
            .testTag("launchpad_cat_${item.category.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isSelected) FameGoGoldContainer else FameGoSurface),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) FameGoGold else FameGoTextSecondary,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = item.title,
                  color = if (isSelected) FameGoWhite else FameGoWhite.copy(alpha = 0.9f),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = item.price,
                  color = FameGoGold,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Text(
                text = item.kit,
                color = FameGoTextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Primary Start Booking CTA Button
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = FameGoGold,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onStartBooking(selectedCategory) }
          .testTag("launchpad_start_booking_btn")
      ) {
        Row(
          modifier = Modifier.padding(vertical = 16.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color(0xFF141518),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Begin Shoot Booking",
            color = Color(0xFF141518),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Trust badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = FameGoSuccessGreen,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "₹15 Lakh Equipment Shield & Verified Operators",
          color = FameGoTextMuted,
          fontSize = 11.sp
        )
      }

      // Clearance for the FameGo rotating wheel
      Spacer(modifier = Modifier.height(145.dp))
    }
  }
}

private data class CategoryLaunchpadItem(
  val category: ShootCategory,
  val title: String,
  val price: String,
  val kit: String,
  val icon: ImageVector
)
