package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.data.SupabaseConfig
import com.example.data.SupabaseNetwork
import com.example.model.Booking
import com.example.model.PaymentStatus
import com.example.ui.components.FameGoButton
import com.example.ui.components.SoftCard
import com.example.ui.theme.FameGoBorderSubtle
import com.example.ui.theme.FameGoBrightGold
import com.example.ui.theme.FameGoCardElevated
import com.example.ui.theme.FameGoDarkerGold
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoLiveRed
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PaymentDemoScreen(
  booking: Booking,
  onPaid: (Booking) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var submitting by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()
  val payContext = LocalContext.current.applicationContext
  Column(
    modifier = modifier.fillMaxSize().background(Color.Transparent).statusBarsPadding()
      .navigationBarsPadding().padding(horizontal = 20.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack, enabled = !submitting) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FameGoTextSecondary)
      }
      Text("Payment", color = FameGoWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
    Text("Demo checkout", color = FameGoGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(start = 48.dp, bottom = 28.dp))
    SoftCard(modifier = Modifier.fillMaxWidth()) {
      Column(Modifier.padding(20.dp)) {
        Text(booking.plan.title, color = FameGoWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(booking.plan.durationLabel, color = FameGoTextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
          Text("Total", color = FameGoTextSecondary, fontSize = 14.sp)
          Text("₹${"%,d".format(booking.priceRupees)}", color = FameGoGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
    Text("No money is charged in this demo. Your booking is created after confirmation.",
      color = FameGoTextMuted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp))
    error?.let { Text(it, color = FameGoLiveRed, fontSize = 13.sp, modifier = Modifier.padding(top = 14.dp)) }
    Spacer(Modifier.weight(1f))
    if (submitting) {
      Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), Arrangement.Center, Alignment.CenterVertically) {
        CircularProgressIndicator(color = FameGoGold, strokeWidth = 2.dp)
        Text("Creating your booking…", color = FameGoTextSecondary, modifier = Modifier.padding(start = 12.dp))
      }
    } else {
      FameGoButton(
        text = "Pay now",
        onClick = {
          submitting = true; error = null
          val paidBooking = booking.copy(
            paymentStatus = PaymentStatus.PAID,
            paymentReference = "DEMO-${System.currentTimeMillis()}"
          )
          scope.launch {
            // Fully online: no local demo fallback. Offline means stop here
            // with a clear message instead of a phantom booking.
            if (!SupabaseNetwork.isDeviceOnline(payContext)) {
              error = "You're offline. Connect to the internet to complete payment."
              submitting = false
              return@launch
            }
            if (!SupabaseConfig.isConfigured) {
              error = "FameGo is temporarily unavailable. Please try again later."
              submitting = false
              return@launch
            }
            FameGoRepository.createPaidBooking(paidBooking)
              .onSuccess { onPaid(it) }
              .onFailure {
                error = FameGoRepository.friendlyMessage(it); submitting = false
              }
          }
        },
        modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp), testTag = "payment_pay_now"
      )
    }
  }
}

/**
 * Fake "payment done" receipt shown until Razorpay is connected.
 * No real money moves; the booking is already marked PAID.
 */
@Composable
fun PaymentSuccessScreen(
  bookingId: String,
  amountRupees: Int,
  planTitle: String,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bookings by FameGoRepository.bookings.collectAsState()
  val booking = remember(bookingId, bookings) {
    bookings.firstOrNull { it.id == bookingId }
  }
  val pop = remember { androidx.compose.animation.core.Animatable(0.6f) }
  val receiptHaptic = LocalHapticFeedback.current
  val receiptSfx = LocalContext.current.applicationContext
  LaunchedEffect(Unit) {
    com.example.ui.components.FameGoHaptics.success(receiptHaptic)
    com.example.data.FameGoSfx.success(receiptSfx)
    pop.animateTo(
      1f,
      androidx.compose.animation.core.spring(
        stiffness = Spring.StiffnessLow,
        dampingRatio = 0.55f
      )
    )
  }
  val spinLoop = rememberInfiniteTransition(label = "sealSpin")
  val spin by spinLoop.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(9000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sealAngle"
  )
  Column(
    modifier = modifier.fillMaxSize().background(Color.Transparent).statusBarsPadding()
      .navigationBarsPadding().padding(horizontal = 20.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(Modifier.height(48.dp))
    // Celebratory seal: slow-orbiting gold dashes around the check.
    val sealDensity = LocalDensity.current
    val sealCx = remember { with(sealDensity) { 42.dp.toPx() } }
    val sealCy = remember { with(sealDensity) { 30.dp.toPx() } }
    val sealRadius = remember { with(sealDensity) { 90.dp.toPx() } }
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(128.dp)
        .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
        .testTag("payment_success_seal")
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f - 4.dp.toPx()
        for (i in 0 until 24) {
          val a = Math.toRadians((spin + i * 15.0).toDouble()).toFloat()
          val inner = r - (if (i % 2 == 0) 10.dp.toPx() else 6.dp.toPx())
          drawLine(
            color = FameGoGold.copy(alpha = if (i % 2 == 0) 0.9f else 0.4f),
            start = Offset(center.x + inner * cos(a), center.y + inner * sin(a)),
            end = Offset(center.x + r * cos(a), center.y + r * sin(a)),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
          )
        }
      }
      Box(
        modifier = Modifier
          .size(84.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(FameGoGold, FameGoDarkerGold),
              center = Offset(sealCx, sealCy),
              radius = sealRadius
            )
          )
          .border(1.5.dp, FameGoBrightGold, CircleShape)
          .shadow(16.dp, CircleShape, spotColor = FameGoGold),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Payment done",
          tint = Color(0xFF1A1408),
          modifier = Modifier.size(40.dp)
        )
      }
    }
    Spacer(Modifier.height(22.dp))
    Text("Payment successful", color = FameGoWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    Text(
      "₹${"%,d".format(amountRupees)} paid via UPI",
      color = FameGoGold, fontSize = 17.sp, fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(top = 6.dp)
    )
    Text(
      "Crew matching starts now — relaxed, your shooter is on the way.",
      color = FameGoTextMuted, fontSize = 13.sp, textAlign = TextAlign.Center,
      lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
    )
    Spacer(Modifier.height(22.dp))
    SoftCard(modifier = Modifier.fillMaxWidth(), isElevated = true) {
      Column(Modifier.padding(20.dp)) {
        ReceiptRow("Booking", booking?.bookingCode?.uppercase() ?: bookingId.take(8).uppercase())
        ReceiptRow("Plan", booking?.plan?.title ?: planTitle)
        booking?.let {
          ReceiptRow("Date", "${it.dateText} • ${it.timeText}")
          ReceiptRow("Venue", it.venueName)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
          Text("Amount paid", color = FameGoTextSecondary, fontSize = 13.sp)
          Text(
            "₹${"%,d".format(amountRupees)}",
            color = FameGoGold, fontSize = 22.sp, fontWeight = FontWeight.Bold
          )
        }
      }
    }
    Spacer(Modifier.height(14.dp))
    Text(
      "Demo mode — no real money moved. Razorpay goes live here soon.",
      color = FameGoTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center
    )
    Spacer(Modifier.weight(1f))
    FameGoButton(
      text = "Find my crew",
      onClick = onContinue,
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      testTag = "payment_success_continue"
    )
    Text(
      text = "Receipt saved to your bookings",
      color = FameGoTextMuted, fontSize = 11.sp,
      modifier = Modifier.padding(bottom = 22.dp)
    )
  }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
  Row(
    Modifier.fillMaxWidth().padding(vertical = 5.dp),
    Arrangement.SpaceBetween, Alignment.CenterVertically
  ) {
    Text(label, color = FameGoTextSecondary, fontSize = 13.sp)
    Text(
      value, color = FameGoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.End, modifier = Modifier.padding(start = 16.dp).weight(1f)
    )
  }
}
