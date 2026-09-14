package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FameGoRepository
import com.example.data.SupabaseAuthClient
import com.example.data.SupabaseRestClient
import com.example.data.SupabaseSession
import com.example.model.BookingStatus
import com.example.model.Booking
import com.example.model.Role
import com.example.model.ShootCategory
import com.example.model.ShootPlan
import com.example.ui.components.FameGoBottomNav
import com.example.ui.components.FameGoTopBar
import com.example.ui.components.FameGoWheelNavigation
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookAShootScreen
import com.example.ui.screens.BookShootLaunchpadScreen
import com.example.ui.screens.ShootPlanScreen
import com.example.ui.screens.PaymentDemoScreen
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
    handleAuthDeepLink(intent)
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
  }

  private fun handleAuthDeepLink(intent: Intent?) {
    val uri: Uri = intent?.data ?: return
    // Only claim our own callback; everything else falls through.
    if (uri.scheme == "famego" && uri.host == "auth") {
      AuthDeepLinkInbox.post(uri.toString())
    }
  }
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
  val pendingLink by AuthDeepLinkInbox.link.collectAsState()
  var linkStatus by remember { mutableStateOf<String?>(null) }
  val appScope = rememberCoroutineScope()

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
    linkStatus = "Confirming your email…"
    val (query, fragment) = AuthDeepLinkInbox.splitParams(uriString)
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
      is Screen.BookAShoot -> currentScreen = Screen.ShootPlans(current.preselectedCategory)
      is Screen.Payment -> currentScreen = Screen.BookAShoot(current.booking.plan, current.booking.category)
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(FameGoBg)
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
            onGetStarted = { currentScreen = Screen.Auth(startInSignUp = true) },
            onSignIn = { currentScreen = Screen.Auth(startInSignUp = false) }
          )
        }

        is Screen.Auth -> {
          AuthScreen(
            initialSignUp = screen.startInSignUp,
            onAuthenticated = { user ->
              FameGoRepository.setCurrentUser(user)
              currentScreen = Screen.Main(tab = "home")
            },
            onBack = { currentScreen = Screen.Welcome },
          )
        }

        is Screen.Main -> {
          Scaffold(
            topBar = {
              FameGoTopBar(
                currentRole = currentUser.role,
                unreadNotifications = notifications.count {
                  it.targetRole == currentUser.role && !it.isRead
                },
                onRoleClick = {},
                onNotificationsClick = { currentScreen = Screen.Main("notifications") },
                onProfileClick = { currentScreen = Screen.Main("profile") },
                roleSwitcherEnabled = false
              )
            },
            containerColor = FameGoBg
          ) { innerPadding ->
            Box(
                modifier = Modifier
                  .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
            ) {
              // Keep back navigation synchronized with the current app screen.
              BackHandler(enabled = screen.tab != "home" && screen.tab != "dashboard") {
                currentScreen = Screen.Main("home")
              }

              val clientTabOrder = listOf("home", "bookings", "book", "notifications", "profile")

              AnimatedContent(
                targetState = screen.tab,
                modifier = Modifier.fillMaxSize()
                  .navigationBarsPadding()
                  .padding(bottom = 142.dp),
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
                    onViewAllBookings = { currentScreen = Screen.Main("bookings") }
                  )
                  "bookings" -> ClientBookingsScreen(
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "bookings")) },
                    onBookAgain = { cat -> navigateTo(Screen.ShootPlans(cat)) },
                    onNewBooking = { navigateTo(Screen.ShootPlans(null)) }
                  )
                  "book" -> BookShootLaunchpadScreen(
                    onStartBooking = { cat -> navigateTo(Screen.ShootPlans(cat)) }
                  )
                  "notifications", "alerts" -> NotificationsScreen(
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "notifications")) }
                  )
                  "profile" -> ClientProfileScreen(
                    onOpenSupport = { navigateTo(Screen.CustomerSupport(Screen.Main("profile"))) },
                    onLogout = {
                      FameGoRepository.logout()
                      screenHistory = emptyList()
                      currentScreen = Screen.Welcome
                    }
                  )
                  else -> ClientHomeScreen(
                    onBookAShoot = { cat -> navigateTo(Screen.ShootPlans(cat)) },
                    onOpenBooking = { id -> navigateTo(Screen.BookingDetails(id, "home")) },
                    onOpenLiveSearch = { id -> navigateTo(Screen.SearchingCrew(id)) },
                    onOpenChat = { id -> navigateTo(Screen.BookingChat(id, Screen.Main(tab))) },
                    onViewAllBookings = { currentScreen = Screen.Main("bookings") }
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
                    "profile" -> CrewProfileScreen(onSwitchRole = { })
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
                    onOpenSupport = { navigateTo(Screen.CustomerSupport(Screen.Main("home"))) }
                  )
                }
              }

              // FameGo rotating wheel navigation (swipe left/right to switch tabs)
              // Crew gets a compact dock so Jobs + Profile are reachable.
              if (currentUser.role == Role.CLIENT) FameGoWheelNavigation(
                currentTab = screen.tab,
                onNavigate = { destination ->
                  currentScreen = Screen.Main(tab = destination)
                },
                onOpenBookingFlow = {
                  navigateTo(Screen.ShootPlans(null))
                },
                modifier = Modifier.align(Alignment.BottomCenter)
              )
              if (currentUser.role == Role.CREW) com.example.ui.components.FlowDock(
                currentRoute = screen.tab,
                onNavigate = { destination -> currentScreen = Screen.Main(tab = destination) },
                modifier = Modifier.align(Alignment.BottomCenter)
              )
            }
          }
        }

        is Screen.ShootPlans -> {
          ShootPlanScreen(
            preselectedCategory = screen.preselectedCategory,
            onContinue = { plan, category -> navigateTo(Screen.BookAShoot(plan, category)) },
            onBack = { goBack(Screen.Main("home")) }
          )
        }

        is Screen.BookAShoot -> {
          BookAShootScreen(
            plan = screen.plan,
            preselectedCategory = screen.preselectedCategory,
            onBookingReadyForPayment = { booking -> navigateTo(Screen.Payment(booking)) },
            onCancel = { goBack(Screen.ShootPlans(screen.preselectedCategory)) }
          )
        }

        is Screen.Payment -> {
          PaymentDemoScreen(
            booking = screen.booking,
            onPaid = { bookingId -> navigateTo(Screen.SearchingCrew(bookingId)) },
            onBack = { goBack(Screen.BookAShoot(screen.booking.plan, screen.booking.category)) }
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
  }
}

// Kept for backward compatibility with test suite
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier, color = FameGoWhite)
}
