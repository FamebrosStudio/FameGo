package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FameGoRepository
import com.example.data.FameGoPush
import com.example.data.SupabaseAuthClient
import com.example.data.SupabaseRestClient
import com.example.data.SupabaseSession
import com.example.model.BookingStatus
import com.example.model.Booking
import com.example.model.Role
import com.example.model.ShootCategory
import com.example.model.ShootPlan
import com.example.ui.components.FameGoTabBar
import com.example.ui.components.FameGoTopBar
import com.example.ui.components.FameGoHaptics
import com.example.ui.components.FameGoSnakeLoader
import com.example.ui.components.FameGoSprings
import com.example.ui.components.swipeDownToDismiss
import com.example.ui.components.swipeToSwitchTabs
import com.example.ui.components.FameGoButton
import com.example.ui.components.fameGoClientTabs
import com.example.ui.components.fameGoCrewTabs
import com.example.ui.components.FameGoAmbientBackground
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookAShootScreen
import com.example.ui.screens.BookShootLaunchpadScreen
import com.example.ui.screens.ShootPlanScreen
import com.example.ui.screens.PaymentDemoScreen
import com.example.ui.screens.PaymentSuccessScreen
import com.example.ui.screens.BookingChatScreen
import com.example.ui.screens.BookingDetailsScreen
import com.example.ui.screens.ClientBookingsScreen
import com.example.ui.screens.ClientHomeScreen
import com.example.ui.screens.ClientProfileScreen
import com.example.ui.screens.CrewHomeScreen
import com.example.ui.screens.CrewJobsScreen
import com.example.ui.screens.CrewProfileScreen
import com.example.ui.screens.CrewRequestDetailScreen
import com.example.ui.screens.CustomerSupportScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SearchingCrewScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.FameGoAccentCyan
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
import com.example.ui.theme.FameGoTheme
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.launch

sealed class Screen {
  object Splash : Screen()
  object Welcome : Screen()
  data class Auth(val startInSignUp: Boolean = false) : Screen()
  data class Main(val tab: String = "home") : Screen()
  data class ShootPlans(val preselectedCategory: ShootCategory? = null) : Screen()
  data class BookAShoot(val plan: ShootPlan, val preselectedCategory: ShootCategory? = null) : Screen()
  data class Payment(val booking: Booking) : Screen()
  data class PaymentSuccess(val bookingId: String, val amountRupees: Int, val planTitle: String) : Screen()
  data class SearchingCrew(val bookingId: String) : Screen()
  data class BookingDetails(val bookingId: String, val returnTab: String = "bookings") : Screen()
  data class CrewRequestDetail(val bookingId: String) : Screen()
  data class BookingChat(val bookingId: String, val returnTo: Screen? = null) : Screen()
  data class CustomerSupport(val returnTo: Screen = Main("profile")) : Screen()
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    SupabaseSession.initialize(applicationContext)
    // Free map tiles (osmdroid) need an app user-agent or tile servers refuse us.
    org.osmdroid.config.Configuration.getInstance().apply {
      userAgentValue = packageName
      load(applicationContext, getPreferences(Context.MODE_PRIVATE))
    }
    handleAuthDeepLink(intent)
    handleBookingAlertIntent(intent)
    enableEdgeToEdge()
    setContent {
      FameGoTheme {
        FameGoApp()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleAuthDeepLink(intent)
    handleBookingAlertIntent(intent)
  }

  private fun handleBookingAlertIntent(intent: Intent?) {
    val bookingId = intent?.getStringExtra(IncomingShootActivity.EXTRA_BOOKING_ID)
      .orEmpty().ifBlank { intent?.getStringExtra("booking_id").orEmpty() }
    if (bookingId.isNotBlank()) BookingAlertInbox.post(bookingId)
  }

  private fun handleAuthDeepLink(intent: Intent?) {
    val uri: Uri = intent?.data ?: return
    // Only claim our own callback; everything else falls through.
    if (uri.scheme == "famego" && uri.host == "auth") {
      AuthDeepLinkInbox.post(uri.toString())
    }
  }
}

/** Holds a tapped shoot-request notification until the Compose tree consumes it. */
object BookingAlertInbox {
  private val _bookingId = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
  val bookingId: kotlinx.coroutines.flow.StateFlow<String?> = _bookingId

  fun post(id: String) { _bookingId.value = id }
  fun consume() { _bookingId.value = null }
}

/** Holds the latest email-confirmation link until the Compose tree consumes it. */
object AuthDeepLinkInbox {
  private val _link = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
  val link: kotlinx.coroutines.flow.StateFlow<String?> = _link

  fun post(uri: String) { _link.value = uri }
  fun consume() { _link.value = null }

  fun splitParams(uriString: String): Pair<Map<String, String>, Map<String, String>> {
    val uri = Uri.parse(uriString)
    val query = mutableMapOf<String, String>()
    uri.queryParameterNames?.forEach { name ->
      uri.getQueryParameter(name)?.let { query[name] = it }
    }
    val fragment = mutableMapOf<String, String>()
    uri.fragment?.split("&")?.forEach { pair ->
      val idx = pair.indexOf('=')
      if (idx > 0) {
        val key = pair.substring(0, idx)
        val value = pair.substring(idx + 1)
        fragment[key] = value
      }
    }
    // Supabase may nest the real params after the deep-link prefix.
    val nested = query["redirect_to"].orEmpty().ifBlank { query["redirectTo"].orEmpty() }
    if (nested.isNotBlank() && (query["token_hash"].isNullOrBlank() && fragment["access_token"].isNullOrBlank())) {
      return splitParams(nested)
    }
    return query to fragment
  }
}

@Composable
fun FameGoApp() {
  var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
  var screenHistory by remember { mutableStateOf(listOf<Screen>()) }
  val currentUser by FameGoRepository.currentUser.collectAsState()
  val notifications by FameGoRepository.notifications.collectAsState()
  val incomingAlert by FameGoRepository.incomingAlert.collectAsState()
  val pendingLink by AuthDeepLinkInbox.link.collectAsState()
  val pendingBookingAlert by BookingAlertInbox.bookingId.collectAsState()
  var linkStatus by remember { mutableStateOf<String?>(null) }
  var showSignOutDialog by remember { mutableStateOf(false) }
  var lastLocalAlertId by remember { mutableStateOf<String?>(null) }
  val isCrewAvailable by FameGoRepository.isCrewAvailable.collectAsState()
  // Brief inter-page shimmer on tab switches only: visible long enough to
  // feel responsive, gone before it can annoy. Never on splash/auth.
  var tabFlash by remember { mutableStateOf(false) }
  val appScope = rememberCoroutineScope()
  val appContext = LocalContext.current
  val appHaptic = LocalHapticFeedback.current
  val notifPermission =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

  // Push registration follows the session: token uploads on sign-in so this
  // phone gets booking alerts, chat pushes and crew-request alerts.
  LaunchedEffect(currentUser.id, currentUser.role) {
    if (currentUser.id.isNotBlank()) {
      FameGoPush.registerToken(appContext, currentUser.id)
      // Crew phones join the broadcast topic for new paid requests.
      FameGoPush.setCrewTopic(appContext, currentUser.role == Role.CREW)
      if (android.os.Build.VERSION.SDK_INT >= 33 &&
        androidx.core.content.ContextCompat.checkSelfPermission(
          appContext, android.Manifest.permission.POST_NOTIFICATIONS
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
      ) {
        notifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Crew dispatch service (Famebook pattern): while an available crew member
  // is signed in, a foreground service polls for paid requests so the popup
  // + notification arrive even with the app closed. Off-duty/logout stops it.
  LaunchedEffect(currentUser.id, currentUser.role, isCrewAvailable) {
    if (currentUser.role == Role.CREW && currentUser.id.isNotBlank() && isCrewAvailable) {
      com.example.data.FameGoDispatchService.start(
        appContext,
        currentUser.id,
        currentUser.name.ifBlank { "FameGo Crew" }
      )
    } else {
      com.example.data.FameGoDispatchService.stop(appContext)
    }
  }

  fun navigateTo(next: Screen, from: Screen = currentScreen) {
    // Main tabs + auth roots reset history; detail screens push.
    val resetsHistory = next is Screen.Welcome || next is Screen.Auth || next is Screen.Main ||
      next is Screen.Splash
    screenHistory = if (resetsHistory) emptyList()
    else (screenHistory + from).takeLast(20)
    currentScreen = next
  }
  fun goBack(fallback: Screen = Screen.Main("home")) {
    val previous = screenHistory.lastOrNull()
    if (previous != null) {
      screenHistory = screenHistory.dropLast(1)
      currentScreen = previous
    } else {
      currentScreen = fallback
    }
  }

  // "Yes, it's me" — user tapped the email link, app opens and confirms automatically.
  LaunchedEffect(pendingLink) {
    val uriString = pendingLink ?: return@LaunchedEffect
    AuthDeepLinkInbox.consume()
    val (query, fragment) = AuthDeepLinkInbox.splitParams(uriString)
    // Bare "Open FameGo app" taps carry no token — just open, don't error.
    if (query["token_hash"].isNullOrBlank() && fragment["token_hash"].isNullOrBlank() &&
      fragment["access_token"].isNullOrBlank()
    ) {
      if (currentScreen is Screen.Splash || currentScreen is Screen.Welcome) {
        currentScreen = Screen.Auth(startInSignUp = false)
      }
      return@LaunchedEffect
    }
    linkStatus = "Confirming your email…"
    val result = SupabaseAuthClient.confirmEmailLink(query, fragment)
    result.onSuccess { auth ->
      // Pull profile so role/name resolve correctly, then drop the user home.
      val raw = SupabaseRestClient.get("profiles?select=*&id=eq.${auth.id}").getOrNull()
      val profile = raw?.let { runCatching { org.json.JSONArray(it).optJSONObject(0) }.getOrNull() }
      val role = runCatching {
        com.example.model.Role.valueOf(profile?.optString("role").orEmpty())
      }.getOrDefault(com.example.model.Role.CLIENT)
      val displayName = profile?.optString("full_name").orEmpty()
        .ifBlank { auth.email.substringBefore('@').ifBlank { "User" } }
      FameGoRepository.setCurrentUser(
        com.example.model.User(
          id = auth.id,
          name = displayName,
          email = auth.email,
          phone = profile?.optString("phone").orEmpty(),
          companyName = profile?.optString("company_name").orEmpty(),
          role = role,
          avatarInitials = displayName.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifEmpty { "FG" }
        )
      )
      linkStatus = null
      screenHistory = emptyList()
      currentScreen = Screen.Main("home")
      linkStatus = "Email verified — welcome to FameGo. You can continue booking."
      appScope.launch {
        kotlinx.coroutines.delay(5000)
        linkStatus = null
      }
    }.onFailure { e ->
      linkStatus = e.message ?: "That link didn't work. Please sign in again."
      // Surface the error on the auth screen so the user can retry.
      if (currentScreen is Screen.Welcome || currentScreen is Screen.Splash) {
        currentScreen = Screen.Auth(startInSignUp = false)
      }
      appScope.launch {
        kotlinx.coroutines.delay(6000)
        linkStatus = null
      }
    }
  }

  // Tapped notification / full-screen popup: crew lands on the request detail.
  LaunchedEffect(pendingBookingAlert, currentUser.role, currentScreen) {
    val bookingId = pendingBookingAlert ?: return@LaunchedEffect
    if (currentScreen is Screen.Splash) return@LaunchedEffect
    if (currentUser.id.isBlank()) return@LaunchedEffect
    BookingAlertInbox.consume()
    if (currentUser.role == Role.CREW) {
      navigateTo(Screen.CrewRequestDetail(bookingId))
    } else {
      navigateTo(Screen.BookingDetails(bookingId, "home"))
    }
  }

  // Foreground realtime alert -> also buzz the status bar with the alarm-style
  // notification, so background phones get heads-up + lock-screen popup even
  // before FCM is configured. Fires once per request.
  LaunchedEffect(incomingAlert?.id, currentUser.role) {
    val alert = incomingAlert ?: return@LaunchedEffect
    if (currentUser.role != Role.CREW) return@LaunchedEffect
    if (lastLocalAlertId == alert.id) return@LaunchedEffect
    lastLocalAlertId = alert.id
    runCatching { FameGoPush.notifyLocalIncoming(appContext, alert) }
  }

  // Inter-page shimmer: Main tab switches flash the mini snake for one beat.
  // Pass-through touches, hard-capped at ~400ms, never on splash/auth flows.
  LaunchedEffect(currentScreen) {
    if (currentScreen is Screen.Main) {
      tabFlash = true
      kotlinx.coroutines.delay(380)
      tabFlash = false
    } else {
      tabFlash = false
    }
  }

  // Handle Android system back button
  BackHandler(
    enabled = currentScreen !is Screen.Splash &&
      currentScreen !is Screen.Main &&
      currentScreen !is Screen.Welcome
  ) {
    when (val current = currentScreen) {
      is Screen.Welcome -> { /* exit or stay */ }
      is Screen.Auth -> currentScreen = Screen.Welcome
      is Screen.ShootPlans -> goBack(Screen.Main("home"))
      is Screen.BookAShoot -> goBack(Screen.ShootPlans(current.preselectedCategory))
      is Screen.Payment -> goBack(Screen.BookAShoot(current.booking.plan, current.booking.category))
      is Screen.PaymentSuccess -> goBack(Screen.Main("home"))
      is Screen.SearchingCrew -> goBack(Screen.Main("home"))
      is Screen.BookingDetails -> goBack(Screen.Main(current.returnTab))
      is Screen.CrewRequestDetail -> goBack(Screen.Main("home"))
      is Screen.BookingChat -> {
        val dest = current.returnTo
        if (dest != null) { screenHistory = screenHistory.dropLast(1); currentScreen = dest }
        else goBack(Screen.Main("home"))
      }
      is Screen.CustomerSupport -> goBack(current.returnTo)
      is Screen.Main -> { /* handled by tab */ }
      Screen.Splash -> {}
    }
  }

  FameGoAmbientBackground(
    modifier = Modifier.fillMaxSize()
  ) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      label = "screenTransition"
    ) { screen ->
      when (screen) {
        is Screen.Splash -> {
          SplashScreen(
            onFinishSplash = { restoredUser ->
              currentScreen = if (restoredUser == null) Screen.Welcome else Screen.Main("home")
            }
          )
        }

        is Screen.Welcome -> {
          WelcomeScreen(
            onGetStarted = { navigateTo(Screen.Auth(startInSignUp = true)) },
            onSignIn = { navigateTo(Screen.Auth(startInSignUp = false)) }
          )
        }

        is Screen.Auth -> {
          AuthScreen(
            initialSignUp = screen.startInSignUp,
            onAuthenticated = { user ->
              FameGoRepository.setCurrentUser(user)
              navigateTo(Screen.Main(tab = "home"))
            },
            onBack = { navigateTo(Screen.Welcome) },
          )
        }

        is Screen.Main -> {
          Scaffold(
            topBar = {
              FameGoTopBar(
                unreadNotifications = notifications.count {
                  it.targetRole == currentUser.role && !it.isRead
                },
                onNotificationsClick = { navigateTo(Screen.Main("notifications")) },
                onProfileClick = { navigateTo(Screen.Main("profile")) }
              )
            },
            containerColor = Color.Transparent
          ) { innerPadding ->
            Box(
                modifier = Modifier
                  .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
            ) {
              // Keep back navigation synchronized with the current app screen.
              BackHandler(enabled = screen.tab != "home" && screen.tab != "dashboard") {
                navigateTo(Screen.Main("home"))
              }

              val clientTabOrder = listOf("home", "bookings", "book", "notifications", "profile")
              // Swipe anywhere on the tab content to move between tabs.
              val swipeOrder = if (currentUser.role == Role.CREW)
                listOf("home", "bookings", "notifications", "profile")
              else clientTabOrder
              fun stepTab(delta: Int) {
                val idx = swipeOrder.indexOf(screen.tab).let { if (it == -1) 0 else it }
                val next = (idx + delta).coerceIn(0, swipeOrder.lastIndex)
                if (next != idx) navigateTo(Screen.Main(tab = swipeOrder[next]))
              }

              AnimatedContent(
                targetState = screen.tab,
                modifier = Modifier.fillMaxSize()
                  .navigationBarsPadding()
                  .padding(bottom = 104.dp)
                  .swipeToSwitchTabs(
                    onSwipeLeft = { stepTab(1) },
                    onSwipeRight = { stepTab(-1) }
                  ),
                transitionSpec = {
                  val initialIdx = clientTabOrder.indexOf(initialState).let { if (it == -1) 0 else it }
                  val targetIdx = clientTabOrder.indexOf(targetState).let { if (it == -1) 0 else it }
                  val direction = if (targetIdx >= initialIdx) 1 else -1
                  (slideInHorizontally(
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                  ) { width -> direction * (width / 3) } + fadeIn(tween(240)))
                    .togetherWith(
                      slideOutHorizontally(
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                      ) { width -> -direction * (width / 3) } + fadeOut(tween(200))
                    )
                },
                label = "clientScreenTransition"
              ) { tab ->
                if (currentUser.role == Role.CLIENT) {
                  when (tab) {
                  "home", "dashboard" -> ClientHomeScreen(
                    onBookAShoot = { cat -> navigateTo(Screen.ShootPlans(cat)) },
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "home")) },
                    onOpenLiveSearch = { id -> navigateTo(Screen.SearchingCrew(id)) },
                    onOpenChat = { id -> navigateTo(Screen.BookingChat(id, Screen.Main(tab))) },
                    onViewAllBookings = { navigateTo(Screen.Main("bookings")) }
                  )
                  "bookings" -> ClientBookingsScreen(
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "bookings")) },
                    onBookAgain = { cat -> navigateTo(Screen.ShootPlans(cat)) },
                    onNewBooking = { navigateTo(Screen.ShootPlans(null)) },
                    onOpenChat = { id -> navigateTo(Screen.BookingChat(id, Screen.Main(tab))) }
                  )
                  "book" -> BookShootLaunchpadScreen(
                    onStartBooking = { cat -> navigateTo(Screen.ShootPlans(cat)) },
                    onPlanClick = { plan ->
                      navigateTo(Screen.BookAShoot(plan, ShootCategory.VIDEO))
                    }
                  )
                  "notifications", "alerts" -> NotificationsScreen(
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "notifications")) }
                  )
                  "profile" -> ClientProfileScreen(
                    onOpenSupport = { navigateTo(Screen.CustomerSupport(Screen.Main("profile"))) },
                    onOpenBookings = { navigateTo(Screen.Main("bookings")) },
                    onLogout = { showSignOutDialog = true }
                  )
                  else -> ClientHomeScreen(
                    onBookAShoot = { cat -> navigateTo(Screen.ShootPlans(cat)) },
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "home")) },
                    onOpenLiveSearch = { id -> navigateTo(Screen.SearchingCrew(id)) },
                    onOpenChat = { id -> navigateTo(Screen.BookingChat(id, Screen.Main(tab))) },
                    onViewAllBookings = { navigateTo(Screen.Main("bookings")) }
                  )
                  }
                } else if (currentUser.role == Role.CREW) {
                  when (tab) {
                    "home" -> CrewHomeScreen(
                      onViewRequestDetail = { id -> navigateTo(Screen.CrewRequestDetail(id)) },
                      onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "home")) },
                      onOpenChat = { id -> navigateTo(Screen.BookingChat(id, Screen.Main(tab))) }
                    )
                    "bookings" -> CrewJobsScreen(onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "bookings")) })
                    "profile" -> CrewProfileScreen(onLogout = { showSignOutDialog = true })
                    "notifications" -> NotificationsScreen(
                      onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "notifications")) }
                    )
                    else -> CrewHomeScreen(
                      onViewRequestDetail = { id -> navigateTo(Screen.CrewRequestDetail(id)) },
                      onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "home")) },
                      onOpenChat = { id -> navigateTo(Screen.BookingChat(id, Screen.Main(tab))) }
                    )
                  }
                } else {
                  AdminDashboardScreen(
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "home")) },
                    onOpenSupport = { navigateTo(Screen.CustomerSupport(Screen.Main("home"))) },
                    onLogout = { showSignOutDialog = true }
                  )
                }
              }

              // Minimal bottom tab bar (Home / Bookings / Book / Alerts / Profile).
              // Crew gets the same bar with a Jobs tab instead of Book.
              if (currentUser.role == Role.CLIENT) FameGoTabBar(
                tabs = fameGoClientTabs(),
                selectedRoute = screen.tab,
                onSelect = { destination -> navigateTo(Screen.Main(tab = destination)) },
                modifier = Modifier.align(Alignment.BottomCenter)
              )
              if (currentUser.role == Role.CREW) FameGoTabBar(
                tabs = fameGoCrewTabs(),
                selectedRoute = screen.tab,
                onSelect = { destination -> navigateTo(Screen.Main(tab = destination)) },
                modifier = Modifier.align(Alignment.BottomCenter)
              )
            }
          }
        }

        is Screen.ShootPlans -> {
          // Booking flows are client-only: crew/admin land back home.
          if (currentUser.role != Role.CLIENT) {
            LaunchedEffect(Unit) { navigateTo(Screen.Main("home")) }
          } else {
          ShootPlanScreen(
            preselectedCategory = screen.preselectedCategory,
            onContinue = { plan, category -> navigateTo(Screen.BookAShoot(plan, category)) },
            onBack = { goBack(Screen.Main("home")) }
          )
          }
        }

        is Screen.BookAShoot -> {
          if (currentUser.role != Role.CLIENT) {
            LaunchedEffect(Unit) { navigateTo(Screen.Main("home")) }
          } else {
          BookAShootScreen(
            plan = screen.plan,
            preselectedCategory = screen.preselectedCategory,
            onBookingReadyForPayment = { booking -> navigateTo(Screen.Payment(booking)) },
            onCancel = { goBack(Screen.ShootPlans(screen.preselectedCategory)) }
          )
          }
        }

        is Screen.Payment -> {
          if (currentUser.role != Role.CLIENT) {
            LaunchedEffect(Unit) { navigateTo(Screen.Main("home")) }
          } else {
          PaymentDemoScreen(
            booking = screen.booking,
            onPaid = { paid ->
              navigateTo(
                Screen.PaymentSuccess(
                  bookingId = paid.id,
                  amountRupees = paid.priceRupees,
                  planTitle = paid.plan.title
                )
              )
            },
            onBack = { goBack(Screen.BookAShoot(screen.booking.plan, screen.booking.category)) }
          )
          }
        }

        is Screen.PaymentSuccess -> {
          PaymentSuccessScreen(
            bookingId = screen.bookingId,
            amountRupees = screen.amountRupees,
            planTitle = screen.planTitle,
            onContinue = { navigateTo(Screen.SearchingCrew(screen.bookingId)) }
          )
        }

        is Screen.SearchingCrew -> {
          SearchingCrewScreen(
            bookingId = screen.bookingId,
            onConfirmed = { navigateTo(Screen.BookingDetails(screen.bookingId, "home")) },
            onCancelSearch = { goBack(Screen.Main("home")) },
            onOpenChat = { bId -> navigateTo(Screen.BookingChat(bId, Screen.SearchingCrew(screen.bookingId))) },
            onOpenDetails = { bId -> navigateTo(Screen.BookingDetails(bId, "home")) }
          )
        }

        is Screen.BookingDetails -> {
          BookingDetailsScreen(
            bookingId = screen.bookingId,
            onBack = { goBack(Screen.Main(screen.returnTab)) },
            onOpenChat = { bId -> navigateTo(Screen.BookingChat(bId, Screen.BookingDetails(screen.bookingId, screen.returnTab))) },
            onRebook = { cat -> navigateTo(Screen.ShootPlans(cat)) },
            onBookSameCrew = { booking -> navigateTo(Screen.Payment(booking)) },
            onContactSupport = { navigateTo(Screen.CustomerSupport(Screen.BookingDetails(screen.bookingId, screen.returnTab))) }
          )
        }

        is Screen.CrewRequestDetail -> {
          CrewRequestDetailScreen(
            requestId = screen.bookingId,
            onAccept = {
              FameGoHaptics.success(appHaptic)
              val me = currentUser
              val profile = FameGoRepository.crewProfiles.value.firstOrNull { it.userId == me.id }
              val crewMember = if (profile != null) {
                com.example.model.AssignedCrewMember(
                  crewId = profile.id,
                  name = profile.fullName.ifBlank { me.name.ifBlank { "FameGo Crew" } },
                  role = profile.primaryRole,
                  phone = profile.phone.ifBlank { me.phone },
                  gear = profile.gearSummary.ifBlank { profile.primaryRole.gearDescription },
                  rating = profile.rating,
                  isVerified = profile.verificationStatus == com.example.model.VerificationStatus.VERIFIED
                )
              } else {
                com.example.model.AssignedCrewMember(
                  crewId = me.id.ifBlank { "crew_${System.currentTimeMillis()}" },
                  name = me.name.ifBlank { "FameGo Crew" },
                  role = com.example.model.CrewRoleType.CINEMATOGRAPHER,
                  phone = me.phone,
                  gear = com.example.model.CrewRoleType.CINEMATOGRAPHER.gearDescription,
                  rating = 4.9,
                  isVerified = false
                )
              }
              FameGoRepository.acceptShootRequest(bookingId = screen.bookingId, crewMember = crewMember)
              navigateTo(Screen.BookingDetails(screen.bookingId, "home"))
            },
            onDecline = {
              FameGoHaptics.micro(appHaptic)
              FameGoRepository.declineShootRequest(screen.bookingId)
              goBack(Screen.Main("home"))
            },
            onBack = { goBack(Screen.Main("home")) }
          )
        }

        is Screen.BookingChat -> {
          BookingChatScreen(
            bookingId = screen.bookingId,
            onBack = {
              val dest = screen.returnTo
              if (dest != null) { screenHistory = screenHistory.dropLast(1); currentScreen = dest }
              else goBack(Screen.Main("home"))
            }
          )
        }

        is Screen.CustomerSupport -> {
          CustomerSupportScreen(
            onBack = { goBack(screen.returnTo) }
          )
        }
      }
    }
    // Inter-page shimmer: mini snake, pass-through touches, auto-gone.
    if (tabFlash && currentScreen is Screen.Main) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color.Black.copy(alpha = 0.45f),
          modifier = Modifier.size(width = 116.dp, height = 72.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            FameGoSnakeLoader(
              modifier = Modifier.size(width = 96.dp, height = 56.dp)
            )
          }
        }
      }
    }
    // Sign-out confirmation — never log out on a stray tap.
    if (showSignOutDialog) {
      AlertDialog(
        onDismissRequest = { showSignOutDialog = false },
        title = { Text("Sign out?", color = FameGoWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
          Text(
            "You'll need your email and password to sign back in.",
            color = FameGoTextSecondary, fontSize = 14.sp
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              showSignOutDialog = false
              FameGoRepository.logout()
              screenHistory = emptyList()
              currentScreen = Screen.Welcome
            }
          ) { Text("Sign out", color = FameGoGold, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
          TextButton(onClick = { showSignOutDialog = false }) {
            Text("Stay", color = FameGoTextSecondary, fontWeight = FontWeight.Medium)
          }
        },
        containerColor = FameGoCard,
        shape = RoundedCornerShape(20.dp)
      )
    }
    // Email-link status pill ("Confirming your email…" / errors).
    linkStatus?.let { status ->
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1A10).copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, FameGoGold.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp).padding(horizontal = 24.dp)
      ) {
        Text(
          text = status,
          color = FameGoWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
      }
    }
    // Crew full-screen incoming shoot request (Famebook-style).
    if (currentUser.role == Role.CREW) {
      incomingAlert?.let { alert ->
        IncomingShootDialog(
          bookingTitle = alert.shootTitle,
          venue = alert.venueName,
          dateTime = "${alert.dateText} • ${alert.timeText}",
          priceRupees = alert.priceRupees,
          onView = {
            FameGoRepository.dismissIncomingAlert()
            navigateTo(Screen.CrewRequestDetail(alert.id))
          },
          onDismiss = { FameGoRepository.dismissIncomingAlert() }
        )
      }
    }
  }
}

@Composable
private fun IncomingShootDialog(
  bookingTitle: String,
  venue: String,
  dateTime: String,
  priceRupees: Int,
  onView: () -> Unit,
  onDismiss: () -> Unit
) {
  val pulseLoop = rememberInfiniteTransition(label = "incomingPulse")
  val pulse by pulseLoop.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "incomingBadge"
  )
  // Spring pop entrance — the card lands with a soft bounce, never rigid.
  val enter = remember { Animatable(0.92f) }
  LaunchedEffect(Unit) { enter.animateTo(1f, FameGoSprings.pop()) }
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.82f))
        .padding(horizontal = 24.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(26.dp),
        color = FameGoCard,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, FameGoGold),
        shadowElevation = 24.dp,
        modifier = Modifier
          .fillMaxWidth()
          .graphicsLayer {
            scaleX = enter.value
            scaleY = enter.value
          }
          .swipeDownToDismiss(onDismiss = onDismiss)
          .testTag("incoming_shoot_dialog")
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = FameGoGold.copy(alpha = 0.16f * pulse + 0.08f)
          ) {
            Text(
              text = "● NEW SHOOT REQUEST",
              color = FameGoGold.copy(alpha = 0.6f * pulse + 0.4f),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = bookingTitle.ifBlank { "Reel Shoot" },
            color = FameGoWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
          Text(
            text = venue,
            color = FameGoTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )
          Text(
            text = dateTime,
            color = FameGoGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
          )
          Text(
            text = "₹${"%,d".format(priceRupees)}",
            color = FameGoWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp)
          )
          Spacer(modifier = Modifier.height(20.dp))
          FameGoButton(
            text = "View request",
            onClick = onView,
            modifier = Modifier.fillMaxWidth(),
            testTag = "incoming_view_request"
          )
          Spacer(modifier = Modifier.height(8.dp))
          TextButton(onClick = onDismiss) {
            Text("Dismiss", color = FameGoTextSecondary, fontSize = 14.sp)
          }
        }
      }
    }
  }
}

// Kept for backward compatibility with test suite
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier, color = FameGoWhite)
}
