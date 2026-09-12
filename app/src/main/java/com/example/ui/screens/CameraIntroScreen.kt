package com.example.ui.screens

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FameGoLogo
import com.example.ui.components.FameGoButton
import com.example.ui.components.FameGoOutlinedButton
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoTextSecondary
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val Context.fameGoIntroDataStore by preferencesDataStore(name = "famego_preferences")

private class IntroPreferenceManager(private val context: Context) {
  private val completed = booleanPreferencesKey("camera_intro_completed")
  private val everyTime = booleanPreferencesKey("show_intro_every_time")
  suspend fun shouldShow(): Boolean {
    val values = context.fameGoIntroDataStore.data.first()
    return values[completed] != true || values[everyTime] == true
  }
  suspend fun save(showEveryTime: Boolean) {
    context.fameGoIntroDataStore.edit { it[completed] = true; it[everyTime] = showEveryTime }
  }
}

@Composable
fun CameraIntroExperience(onFinished: () -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var checking by remember { mutableStateOf(true) }
  var showIntro by remember { mutableStateOf(false) }
  var showPreference by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    showIntro = IntroPreferenceManager(context).shouldShow()
    checking = false
    if (!showIntro) onFinished()
  }
  if (checking) return
  if (showPreference) {
    IntroPreferenceScreen(
      onSave = { everyTime ->
        showPreference = false
        // The preference screen owns the suspend write and calls completion afterwards.
        onFinished()
      }, modifier = modifier
    )
  } else if (showIntro) {
    CameraIntroScreen(onComplete = { showPreference = true }, modifier = modifier)
  }
}

@Composable
private fun IntroPreferenceScreen(onSave: (Boolean) -> Unit, modifier: Modifier = Modifier) {
  var everyTime by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  Column(
    modifier = modifier.fillMaxSize().background(FameGoBg).padding(28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    FameGoLogo(modifier = Modifier.width(230.dp).height(150.dp))
    Spacer(Modifier.height(28.dp))
    Text("How would you like FameGo to open?", color = FameGoWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(22.dp))
    PreferenceChoice("Show this intro every time", everyTime) { everyTime = true }
    Spacer(Modifier.height(10.dp))
    PreferenceChoice("Show this only once", !everyTime) { everyTime = false }
    Spacer(Modifier.height(26.dp))
    FameGoButton("Save Preference", { scope.launch { IntroPreferenceManager(context).save(everyTime); onSave(everyTime) } }, modifier = Modifier.width(220.dp))
  }
}

@Composable
private fun PreferenceChoice(text: String, selected: Boolean, onClick: () -> Unit) {
  val color = if (selected) FameGoGold else FameGoTextSecondary
  androidx.compose.material3.Surface(
    onClick = onClick, shape = RoundedCornerShape(14.dp), color = if (selected) FameGoGold.copy(alpha = .14f) else Color(0xFF111317),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = if (selected) 1f else .35f)), modifier = Modifier.width(300.dp)
  ) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Text(text, color = color, fontSize = 14.sp) } }
}

@Composable
private fun CameraIntroScreen(onComplete: () -> Unit, modifier: Modifier = Modifier) {
  var progress by remember { mutableFloatStateOf(0f) }
  var dragging by remember { mutableStateOf(false) }
  var captured by remember { mutableStateOf(false) }
  var showBrand by remember { mutableStateOf(false) }
  var showSkip by remember { mutableStateOf(false) }
  var size by remember { mutableStateOf(IntSize.Zero) }
  var previousAngle by remember { mutableStateOf<Float?>(null) }
  val reducedMotion = false
  val animatedProgress by animateFloatAsState(progress, tween(if (dragging) 70 else 180, easing = FastOutSlowInEasing), label = "lensProgress")

  LaunchedEffect(Unit) {
    delay(1000)
    showSkip = true
    delay(4000)
    // Safety valve: the user can always bypass the interaction.
    if (!captured && progress < 1f) { progress = 1f }
  }
  LaunchedEffect(progress) {
    if (progress >= 0.999f && !captured) {
      captured = true
      delay(300)
      if (!reducedMotion) delay(240)
      showBrand = true
      delay(if (reducedMotion) 180 else 520)
      onComplete()
    }
  }

  Box(modifier.fillMaxSize().background(FameGoBg), contentAlignment = Alignment.Center) {
    Canvas(
      Modifier.size(230.dp).onSizeChanged { size = it }.pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { dragging = true },
          onDrag = { change, _ ->
            change.consume()
            if (size == IntSize.Zero) return@detectDragGestures
            val center = Offset(size.width / 2f, size.height / 2f)
            val point = change.position - center
            val angle = atan2(point.y, point.x)
            val last = previousAngle
            if (last != null) {
              var delta = angle - last
              if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
              if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()
              progress = (progress + (if (delta > 0) delta else delta * .12f) / (Math.PI.toFloat() * 2f)).coerceIn(0f, 1f)
            }
            previousAngle = angle
          },
          onDragEnd = { dragging = false; previousAngle = null },
          onDragCancel = { dragging = false; previousAngle = null }
        )
      }
    ) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = minOf(size.width, size.height) * .39f
      drawCircle(Color(0xFF111317), radius + 22f)
      drawCircle(Brush.radialGradient(listOf(Color(0xFF1A1D23), Color(0xFF070809))), radius - 10f)
      drawCircle(Color(0x553A3F49), radius + 16f, style = Stroke(2f))
      drawArc(Brush.sweepGradient(listOf(FameGoGold, Color(0xFFFFC857), FameGoGold)), -90f, animatedProgress * 360f, false, center - Offset(radius, radius), androidx.compose.ui.geometry.Size(radius * 2, radius * 2), style = Stroke(7f, cap = StrokeCap.Round))
      drawCircle(Color(0x66FFFFFF), radius - 20f, style = Stroke(1.5f))
      drawCircle(Color(0x221FFFFF), radius - 34f)
      if (captured) drawCircle(Color.White.copy(alpha = if (showBrand) .06f else .2f), radius - 8f)
    }
    AnimatedVisibility(showBrand, enter = fadeIn(tween(300)), exit = fadeOut()) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("FameGo", color = FameGoWhite, fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.megrim_regular)), fontSize = 34.sp)
        Text("by Famebros Studio", color = FameGoTextMuted, fontSize = 11.sp)
      }
    }
    Text("Rotate to enter", color = FameGoTextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 62.dp))
    if (showSkip && !captured) {
      Text("Skip", color = FameGoTextMuted, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(22.dp).clickable { progress = 1f })
    }
  }
}
