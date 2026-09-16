package com.example.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import com.example.model.Role
import com.example.model.User
import com.example.data.SupabaseAuthClient
import com.example.data.SupabaseNetwork
import com.example.data.SupabaseRestClient
import com.example.data.FameGoRepository
import com.example.ui.components.FameGoButton
import com.example.ui.components.FameGoWordmark
import com.example.ui.components.FameGoOutlinedButton
import com.example.ui.theme.FameGoBg
import com.example.ui.theme.FameGoBorder
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.animation.core.tween

@Composable
fun SplashScreen(
  onFinishSplash: (User?) -> Unit,
  modifier: Modifier = Modifier
) {
  val reveal = remember { Animatable(0f) }
  val finish by rememberUpdatedState(onFinishSplash)
  LaunchedEffect(Unit) {
    val startedAt = SystemClock.elapsedRealtime()
    launch { reveal.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
    // Generous restore budget: slow networks still stay signed in instead of
    // bouncing to Welcome (offline falls back to the cached profile fast).
    val restoredUser = withTimeoutOrNull(7000) {
      runCatching { FameGoRepository.restoreSignedInUser() }.getOrNull()
    }
    val remaining = 2600L - (SystemClock.elapsedRealtime() - startedAt)
    if (remaining > 0) delay(remaining)
    finish(restoredUser)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
      .testTag("cinematic_splash"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val progress = reveal.value
      // A quiet pool of light and four viewfinder corners settle into focus.
      drawRect(Brush.radialGradient(
        listOf(Color(0xFF252017).copy(alpha = progress * 0.55f), Color.Transparent),
        center = center, radius = size.minDimension * 0.8f
      ))
      val halfWidth = size.width * (0.37f - progress * 0.04f)
      val halfHeight = size.minDimension * (0.20f - progress * 0.03f)
      val corner = 12.dp.toPx()
      val ink = FameGoGold.copy(alpha = progress * 0.40f)
      for (x in listOf(-1f, 1f)) {
        for (y in listOf(-1f, 1f)) {
          val point = center + Offset(x * halfWidth, y * halfHeight)
          drawLine(ink, point, point - Offset(x * corner, 0f), 1.dp.toPx())
          drawLine(ink, point, point - Offset(0f, y * corner), 1.dp.toPx())
        }
      }
    }
    Column(
      modifier = Modifier.graphicsLayer {
        alpha = reveal.value
        scaleX = 1.035f - reveal.value * 0.035f
        scaleY = scaleX
      },
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("FameGo", color = FameGoWhite,
        fontFamily = FontFamily(Font(com.example.R.font.megrim_regular)),
        fontSize = 42.sp, letterSpacing = 1.sp)
      Spacer(modifier = Modifier.height(14.dp))
      Text("FAMEBROS STUDIO", color = FameGoTextSecondary,
        fontSize = 9.sp, letterSpacing = 3.sp)
    }
  }
}

@Composable
fun WelcomeScreen(
  onGetStarted: () -> Unit,
  onSignIn: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Brand Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        FameGoWordmark(modifier = Modifier.width(108.dp))
        Text(
          text = "by Famebros Studio",
          color = FameGoTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }

      // Center Visual & Typography
      Column(
        modifier = Modifier.padding(vertical = 32.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = FameGoCardElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder),
          modifier = Modifier.padding(bottom = 28.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(FameGoGold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Verified film and photo crew on demand",
              color = FameGoGold,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Text(
          text = "Book cinema crew in minutes",
          color = FameGoWhite,
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          lineHeight = 40.sp,
          letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Book verified cinematographers, photographers, drone operators and editors for your next shoot. Simple, fast and reliable.",
          color = FameGoTextSecondary,
          fontSize = 15.sp,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Three quick value badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ValuePill(title = "Verified crew")
          ValuePill(title = "Call sheets")
          ValuePill(title = "Quick booking")
        }
      }

      // Bottom Actions
      Column(
        modifier = Modifier.padding(bottom = 24.dp)
      ) {
        FameGoButton(
          text = "Get started",
          onClick = onGetStarted,
          icon = Icons.AutoMirrored.Filled.ArrowForward,
          modifier = Modifier.fillMaxWidth(),
          testTag = "welcome_get_started"
        )

        Spacer(modifier = Modifier.height(12.dp))

        FameGoOutlinedButton(
          text = "Sign in",
          onClick = onSignIn,
          modifier = Modifier.fillMaxWidth(),
          testTag = "welcome_sign_in"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Famebros Studio • Mumbai",
          color = FameGoTextMuted,
          fontSize = 11.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun ValuePill(title: String) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = FameGoSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, FameGoBorder)
  ) {
    Text(
      text = title,
      color = FameGoTextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

@Composable
fun AuthScreen(
  initialSignUp: Boolean = false,
  onAuthenticated: (User) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSignUp by remember(initialSignUp) { mutableStateOf(initialSignUp) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var fullName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  // Entry mode: clients sign in, crew applies via the shoot-crew form.
  var authMode by remember { mutableStateOf("client") }
  var isSubmitting by remember { mutableStateOf(false) }
  var authError by remember { mutableStateOf<String?>(null) }
  var authNotice by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val appContext = LocalContext.current
  val authHaptic = LocalHapticFeedback.current
  LaunchedEffect(authError) {
    if (authError != null) com.example.ui.components.FameGoHaptics.error(authHaptic, coroutineScope)
  }
  LaunchedEffect(authNotice) {
    if (authNotice != null) com.example.ui.components.FameGoHaptics.success(authHaptic)
  }

  val scrollState = rememberScrollState()
  val emailValid = email.trim().contains("@") && email.trim().contains(".")
  val passwordValid = password.length >= 6
  val signUpValid = fullName.trim().length >= 2 && phone.filter(Char::isDigit).length >= 10 &&
    password == confirmPassword
  val canSubmit = !isSubmitting && emailValid && passwordValid && (!isSignUp || signUpValid)

  fun submit() {
    authError = null
    authNotice = null
    if (!canSubmit) {
      authError = if (!emailValid) "Enter a valid email address."
        else if (!passwordValid) "Password must be at least 6 characters."
        else if (isSignUp && phone.filter(Char::isDigit).length < 10) "Enter a valid phone number."
        else if (isSignUp && password != confirmPassword) "Passwords do not match."
        else "Please complete all required fields."
      return
    }
    if (!com.example.data.SupabaseConfig.isConfigured) {
      authError = "FameGo is temporarily unavailable. Please try again later."
      return
    }
    if (!SupabaseNetwork.isDeviceOnline(appContext)) {
      authError = "You're offline. Turn on mobile data or Wi-Fi and try again."
      return
    }
    isSubmitting = true
    coroutineScope.launch {
      val result = SupabaseAuthClient.authenticate(
        email = email.trim(), password = password, signUp = isSignUp,
        name = fullName.trim(), phone = phone.trim(), role = Role.CLIENT.name,
        companyName = companyName.trim()
      )
      isSubmitting = false
      result.onSuccess { auth ->
        val profile = SupabaseRestClient.get("profiles?select=*&id=eq.${auth.id}").getOrNull()
        val profileJson = profile?.let { runCatching { org.json.JSONArray(it).optJSONObject(0) }.getOrNull() }
        val displayName = profileJson?.optString("full_name").orEmpty()
          .ifEmpty { fullName.trim() }
          .ifEmpty { email.substringBefore("@").ifEmpty { "User" } }
        val resolvedRole = runCatching { Role.valueOf(profileJson?.optString("role").orEmpty()) }
          .getOrDefault(Role.CLIENT)
        onAuthenticated(User(
          id = auth.id, name = displayName, email = auth.email,
          phone = profileJson?.optString("phone").orEmpty().ifEmpty { phone.trim() },
          companyName = profileJson?.optString("company_name").orEmpty().ifEmpty { companyName.trim() },
          role = resolvedRole,
          avatarInitials = displayName.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifEmpty { "FG" }
        ))
      }.onFailure { e ->
        if (!SupabaseNetwork.isDeviceOnline(appContext)) {
          authError = "You're offline. Reconnect, then tap \"${if (isSignUp) "Create account" else "Sign in"}\" again."
        } else {
          val msg = e.message ?: "Authentication failed"
          if (msg.contains("CHECK_EMAIL") || msg.contains("confirm your email", ignoreCase = true)) {
            authNotice = "Account created. Open your email and tap \"Yes, it's me\" — the FameGo app will confirm you automatically, then sign in."
            isSignUp = false
          } else {
            authError = SupabaseAuthClient.friendlyMessage(e, signingUp = isSignUp)
          }
        }
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
      .statusBarsPadding()
      .navigationBarsPadding()
      .imePadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.clickable { onBack() },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Back",
            color = FameGoTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
        }

        FameGoWordmark(modifier = Modifier.width(96.dp))
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Heading
      Text(
        text = if (isSignUp) "Create account" else "Sign in",
        color = FameGoWhite,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = if (isSignUp) "Join FameGo to book verified crew in minutes."
          else "Welcome back. Sign in to your account.",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Client vs Crew choice lives ONLY on account creation.
      // Sign-in is just email + password for everyone.
      if (isSignUp) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AuthModeCard(
            title = "Client",
            subtitle = "Book reel shoots",
            icon = Icons.Default.Person,
            selected = authMode == "client",
            onClick = { authMode = "client" },
            modifier = Modifier.weight(1f),
            testTag = "auth_mode_client"
          )
          AuthModeCard(
            title = "Be a Crew",
            subtitle = "Shoot reels, earn",
            icon = Icons.Default.Videocam,
            selected = authMode == "crew",
            onClick = { authMode = "crew" },
            modifier = Modifier.weight(1f),
            testTag = "auth_mode_crew"
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      if (isSignUp && authMode == "crew") {
        CrewApplicationForm(
          onExplore = {
            // Guest preview while under review — session-only, nothing syncs.
            onAuthenticated(
              User(
                id = "",
                name = "Guest",
                email = "",
                phone = "",
                role = Role.CLIENT,
                avatarInitials = "GU"
              )
            )
          }
        )
      } else {

      // Input Fields
      if (isSignUp) {
        FameGoTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = "Full name",
          icon = Icons.Default.Person,
          keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = phone,
          onValueChange = { phone = it },
          label = "Phone",
          icon = Icons.Default.Phone,
          keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = companyName,
          onValueChange = { companyName = it },
          label = "Company (optional)",
          icon = Icons.Default.Business,
          keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      FameGoTextField(
        value = email,
        onValueChange = { email = it },
        label = "Email",
        icon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
      )

      Spacer(modifier = Modifier.height(12.dp))

      FameGoTextField(
        value = password,
        onValueChange = { password = it },
        label = "Password (min 6 chars)",
        icon = Icons.Default.Lock,
        isPassword = true
      )

      if (isSignUp) {
        Spacer(modifier = Modifier.height(12.dp))
        FameGoTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it },
          label = "Confirm password",
          icon = Icons.Default.Lock,
          isPassword = true
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Primary CTA
      FameGoButton(
        text = if (isSignUp) "Create account" else "Sign in",
        onClick = { submit() },
        enabled = canSubmit,
        modifier = Modifier.fillMaxWidth(),
        testTag = "auth_submit_button"
      )

      // Quiet inline progress — no full-screen loader on sign-in.
      if (isSubmitting) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          androidx.compose.material3.CircularProgressIndicator(
            color = FameGoGold,
            strokeWidth = 2.dp,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isSignUp) "Creating your account…" else "Signing you in…",
            color = FameGoTextSecondary,
            fontSize = 12.sp
          )
        }
      }

      authError?.let { message ->
        Text(message, color = Color(0xFFFF7B84), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
      }
      authNotice?.let { message ->
        Text(message, color = FameGoGold, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth().clickable {
          isSignUp = !isSignUp
          authMode = "client"
          authError = null
          authNotice = null
        },
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (isSignUp) "Have an account? Sign in" else "New here? Create account",
          color = FameGoGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(vertical = 8.dp)
        )
      }

      } // end client auth branch

      Spacer(modifier = Modifier.height(20.dp))

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
fun FameGoTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isPassword: Boolean = false,
  keyboardType: KeyboardType = KeyboardType.Text
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, color = FameGoTextMuted, fontSize = 13.sp) },
    leadingIcon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = FameGoTextMuted,
        modifier = Modifier.size(18.dp)
      )
    },
    visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    singleLine = true,
    shape = RoundedCornerShape(14.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedTextColor = FameGoTextPrimary,
      unfocusedTextColor = FameGoTextPrimary,
      focusedContainerColor = FameGoCard,
      unfocusedContainerColor = FameGoCard,
      focusedBorderColor = FameGoGold,
      unfocusedBorderColor = FameGoBorder,
      cursorColor = FameGoGold
    ),
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
private fun AuthModeCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "auth_mode"
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (selected) FameGoGoldContainer else FameGoCard,
    border = androidx.compose.foundation.BorderStroke(
      1.dp, if (selected) FameGoGold else FameGoBorder
    ),
    modifier = modifier.clickable { onClick() }.testTag(testTag)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) FameGoGold else FameGoTextMuted,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        color = if (selected) FameGoWhite else FameGoTextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
      Text(
        text = subtitle,
        color = if (selected) FameGoGold else FameGoTextMuted,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

/**
 * Famebros Studio Shoot Crew Application — iPhone Reel Content Shooter.
 * Every field is required; the form posts to the support inbox for review.
 */
@Composable
private fun CrewApplicationForm(
  onExplore: () -> Unit,
  modifier: Modifier = Modifier
) {
  var fullName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("") }
  var iphoneModel by remember { mutableStateOf("") }
  var portfolioLink by remember { mutableStateOf("") }
  var instagram by remember { mutableStateOf("") }
  var experience by remember { mutableStateOf("") }
  var bestShoot by remember { mutableStateOf("") }
  var hasIphone by remember { mutableStateOf(false) }
  var hasGimbal by remember { mutableStateOf(false) }
  var hasMic by remember { mutableStateOf(false) }
  var hasLight by remember { mutableStateOf(false) }
  var hasPowerBank by remember { mutableStateOf(false) }
  var formError by remember { mutableStateOf<String?>(null) }
  var isSending by remember { mutableStateOf(false) }
  var submitted by remember { mutableStateOf(false) }
  val formScope = rememberCoroutineScope()

  if (submitted) {
    Column(
      modifier = modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(12.dp))
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = FameGoSuccessGreen,
        modifier = Modifier.size(64.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Application received — under review",
        color = FameGoWhite,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
      Text(
        text = "Our team will call you for a short interview about your reel shoots, gear and location. After approval you join the Famebros Shoot Crew.",
        color = FameGoTextSecondary,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "What we check on the call: previous brand/creator reels, vertical video sense, camera handling, shot quality, gear and city availability.",
        color = FameGoTextMuted,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(24.dp))
      FameGoButton(
        text = "Explore the app",
        onClick = onExplore,
        modifier = Modifier.fillMaxWidth(),
        testTag = "crew_explore_button"
      )
      Text(
        text = "Look around while you wait — booking unlocks after approval.",
        color = FameGoTextMuted,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 10.dp)
      )
    }
    return
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "Shoot Crew Application",
      color = FameGoWhite,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "iPhone Reel Content Shooter (vertical only — no photo, no editing). Fill everything, our team calls shortlisted shooters.",
      color = FameGoTextSecondary,
      fontSize = 12.sp,
      lineHeight = 18.sp,
      modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )

    FameGoTextField(value = fullName, onValueChange = { fullName = it }, label = "Full name *", icon = Icons.Default.Person)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = phone, onValueChange = { phone = it }, label = "Phone *", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = email, onValueChange = { email = it }, label = "Email *", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = city, onValueChange = { city = it }, label = "City / Location *", icon = Icons.Default.Person)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = iphoneModel, onValueChange = { iphoneModel = it }, label = "iPhone model (14 Pro / 15 Pro or above) *", icon = Icons.Default.Videocam)

    Spacer(modifier = Modifier.height(20.dp))
    Text(text = "Required equipment *", color = FameGoTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    CrewGearCheck(label = "iPhone (14 Pro / 15 Pro or above)", checked = hasIphone, onToggle = { hasIphone = !hasIphone })
    CrewGearCheck(label = "Mobile Gimbal / Stabilizer", checked = hasGimbal, onToggle = { hasGimbal = !hasGimbal })
    CrewGearCheck(label = "Wireless Mic", checked = hasMic, onToggle = { hasMic = !hasMic })
    CrewGearCheck(label = "LED Light", checked = hasLight, onToggle = { hasLight = !hasLight })
    CrewGearCheck(label = "Power Bank", checked = hasPowerBank, onToggle = { hasPowerBank = !hasPowerBank })

    Spacer(modifier = Modifier.height(20.dp))
    FameGoTextField(value = portfolioLink, onValueChange = { portfolioLink = it }, label = "Portfolio / reel work link *", icon = Icons.Default.Email)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = instagram, onValueChange = { instagram = it }, label = "Instagram handle *", icon = Icons.Default.Person)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = experience, onValueChange = { experience = it.filter(Char::isDigit).take(2) }, label = "Years shooting reels *", icon = Icons.Default.Person, keyboardType = KeyboardType.Number)
    Spacer(modifier = Modifier.height(12.dp))
    FameGoTextField(value = bestShoot, onValueChange = { bestShoot = it }, label = "Best reel shoot you did *", icon = Icons.Default.Person)

    Spacer(modifier = Modifier.height(24.dp))
    FameGoButton(
      text = if (isSending) "Sending…" else "Submit application",
      onClick = {
        if (isSending) return@FameGoButton
        formError = null
        val emailOk = email.trim().contains("@") && email.trim().contains(".")
        val missing = mutableListOf<String>()
        if (fullName.trim().length < 2) missing += "name"
        if (phone.filter(Char::isDigit).length < 10) missing += "phone"
        if (!emailOk) missing += "email"
        if (city.trim().isEmpty()) missing += "city"
        if (iphoneModel.trim().isEmpty()) missing += "iPhone model"
        if (portfolioLink.trim().isEmpty()) missing += "portfolio link"
        if (instagram.trim().isEmpty()) missing += "Instagram"
        if (experience.trim().isEmpty()) missing += "experience"
        if (bestShoot.trim().isEmpty()) missing += "best shoot"
        if (!(hasIphone && hasGimbal && hasMic && hasLight && hasPowerBank)) missing += "all 5 gear items"
        if (missing.isNotEmpty()) {
          formError = "Please complete: ${missing.joinToString(", ")}."
          return@FameGoButton
        }
        isSending = true
        formScope.launch {
          FameGoRepository.submitCrewApplication(
            fullName = fullName,
            phone = phone,
            email = email,
            city = city,
            iphoneModel = iphoneModel,
            portfolioUrl = portfolioLink,
            instagram = instagram,
            experienceYears = experience.toIntOrNull() ?: 0,
            bestShoot = bestShoot
          ).onSuccess { submitted = true }
            .onFailure { formError = FameGoRepository.friendlyMessage(it) }
          isSending = false
        }
      },
      enabled = !isSending,
      modifier = Modifier.fillMaxWidth(),
      testTag = "crew_apply_submit"
    )
    formError?.let { message ->
      Text(message, color = Color(0xFFFF7B84), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
    }
  }
}

@Composable
private fun CrewGearCheck(label: String, checked: Boolean, onToggle: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (checked) FameGoGoldContainer else FameGoCard,
    border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) FameGoGold else FameGoBorder),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clickable { onToggle() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(20.dp)
          .clip(androidx.compose.foundation.shape.CircleShape)
          .background(if (checked) FameGoGold else androidx.compose.ui.graphics.Color.Transparent)
          .border(1.5.dp, if (checked) FameGoGold else FameGoTextMuted, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
      ) {
        if (checked) {
          Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1A1408), modifier = Modifier.size(14.dp))
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(text = label, color = if (checked) FameGoWhite else FameGoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
  }
}
