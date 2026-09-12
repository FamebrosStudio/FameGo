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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.ui.components.SoftCard
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
import com.example.ui.theme.fameGoRise

@Composable
fun ClientProfileScreen(
  onOpenSupport: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentUser by FameGoRepository.currentUser.collectAsState()
  val scrollState = rememberScrollState()
  var activeModal by remember { mutableStateOf<String?>(null) }

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
        Text(
          text = "Producer Profile",
          color = FameGoWhite,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.5).sp
        )
        Text(
          text = "Sign out",
          color = FameGoTextMuted,
          fontSize = 12.sp,
          modifier = Modifier
            .clickable { onLogout() }
            .testTag("profile_logout_button")
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Avatar & Identity Card
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(FameGoCardElevated)
            .border(1.5.dp, FameGoGold.copy(alpha = 0.6f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = currentUser.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
            color = FameGoGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = currentUser.name,
              color = FameGoWhite,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(FameGoGoldContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "PRODUCER",
                color = FameGoGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = currentUser.company ?: "Independent Production Studio",
            color = FameGoTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp)
          )

          Text(
            text = "Mumbai, MH • Account Verified",
            color = FameGoSuccessGreen,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Section 1: Account
      Text(
        text = "Account",
        color = FameGoTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))

      SoftCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          ProfileClickableRow("Email", currentUser.email) { activeModal = "email" }
          ProfileClickableRow("Phone", currentUser.phone) { activeModal = "phone" }
          ProfileClickableRow("Billing details", "Not added") { activeModal = "billing" }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section 2: Production
      Text(
        text = "Production",
        color = FameGoTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))

      SoftCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          ProfileClickableRow("Saved crew", "No saved crew") { activeModal = "crew" }
          ProfileClickableRow("Saved locations", "No saved locations") { activeModal = "locations" }
          ProfileClickableRow("Storage folder", "Connect storage") { activeModal = "storage" }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section 3: Help and safety
      Text(
        text = "Help and safety",
        color = FameGoTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))

      SoftCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          ProfileClickableRow("Help desk", "24/7 support", onClick = onOpenSupport)
          ProfileClickableRow("Cancellation policy", "Flexible 12h") { activeModal = "cancellation" }
          ProfileClickableRow("Equipment insurance", "Covered up to ₹15 Lakhs") { activeModal = "insurance" }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Spacer(modifier = Modifier.height(130.dp))
    }

    // Interactive Modals for every clicked row
    activeModal?.let { modal ->
      AlertDialog(
        onDismissRequest = { activeModal = null },
        title = {
          Text(
            text = when (modal) {
              "email" -> "Verified Email Address"
              "phone" -> "Phone & SMS Alerts"
              "billing" -> "Tax & Billing Profile"
              "crew" -> "Bookmarked Crew Members"
              "locations" -> "Saved Studios & Locations"
              "storage" -> "Famebros Cloud Media Vault"
              "cancellation" -> "Cancellation & Refund Terms"
              "insurance" -> "Studio Equipment Insurance"
              else -> "Account Setting"
            },
            color = FameGoWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (modal) {
              "email" -> {
                Text(
                  text = "Your account email is ${currentUser.email}. All invoices, booking confirmations, and media delivery links are dispatched to this address.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp
                )
                Text(
                  text = "Status: 2-Factor Verified",
                  color = FameGoSuccessGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
              "phone" -> {
                Text(
                  text = "Primary contact: ${currentUser.phone}. Crew coordinators and assigned cinematographers receive direct callsheet communication through this number.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp
                )
              }
              "billing" -> {
                Text(
                  text = "Company: ${currentUser.company ?: "Independent Producer"}",
                  color = FameGoWhite,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Your billing details will appear here after they are connected.",
                  color = FameGoTextSecondary,
                  fontSize = 12.sp
                )
              }
              "crew" -> {
                Text(
                  text = "Saved crew members will appear here when you bookmark them.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp,
                  lineHeight = 20.sp
                )
              }
              "locations" -> {
                Text(
                  text = "Saved locations will appear here after you add them.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp,
                  lineHeight = 20.sp
                )
              }
              "storage" -> {
                Text(
                  text = "Your connected storage and delivered media will appear here.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp
                )
              }
              "cancellation" -> {
                Text(
                  text = "• Free cancellation up to 12 hours before call time.\n• Within 12 hours: 50% crew compensation fee.\n• Within 2 hours or on-set call: Full day rate applies to protect crew scheduling.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
              "insurance" -> {
                Text(
                  text = "FameGo Production Shield: All active shoots carry ₹15,00,000 comprehensive equipment & transit insurance covering cameras, lenses, lighting rigs, and gimbals against accidental damage or loss.",
                  color = FameGoTextSecondary,
                  fontSize = 13.sp
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { activeModal = null }) {
            Text("Done", color = FameGoGold, fontWeight = FontWeight.Bold)
          }
        },
        containerColor = FameGoCard,
        shape = RoundedCornerShape(20.dp)
      )
    }
  }
}

@Composable
private fun ProfileClickableRow(
  label: String,
  value: String,
  onClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = onClick != null) { onClick?.invoke() }
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = FameGoWhite,
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = value,
        color = FameGoTextMuted,
        fontSize = 13.sp
      )
      if (onClick != null) {
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = FameGoTextMuted,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
