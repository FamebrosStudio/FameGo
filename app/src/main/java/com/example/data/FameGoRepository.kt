package com.example.data

import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.CrewApplication
import com.example.model.CrewApplicationStatus
import com.example.model.CrewProfile
import com.example.model.CrewRating
import com.example.model.LiveCrewPoint
import com.example.model.NotificationItem
import com.example.model.PaymentStatus
import com.example.model.Role
import com.example.model.SavedLocation
import com.example.model.User
import com.example.model.VerificationStatus
import com.example.model.ShootPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** In-memory UI state boundary. Supabase can replace this without screen changes. */
object FameGoRepository {
  private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val _currentUser = MutableStateFlow(User(id = "", name = "", email = "", phone = ""))
  val currentUser: StateFlow<User> = _currentUser.asStateFlow()
  private val _activeRole = MutableStateFlow(Role.CLIENT)
  val activeRole: StateFlow<Role> = _activeRole.asStateFlow()
  private val _activeSearchingBookingId = MutableStateFlow<String?>(null)
  val activeSearchingBookingId: StateFlow<String?> = _activeSearchingBookingId.asStateFlow()
  val savedLocations: List<SavedLocation> = emptyList()
  private val _crewProfiles = MutableStateFlow<List<CrewProfile>>(emptyList())
  val crewProfiles: StateFlow<List<CrewProfile>> = _crewProfiles.asStateFlow()
  private val _ratings = MutableStateFlow<Map<String, CrewRating>>(emptyMap())
  val ratings: StateFlow<Map<String, CrewRating>> = _ratings.asStateFlow()
  private val _liveSharing = MutableStateFlow<Set<String>>(emptySet())
  val liveSharing: StateFlow<Set<String>> = _liveSharing.asStateFlow()
  private val _livePoints = MutableStateFlow<Map<String, LiveCrewPoint>>(emptyMap())
  val livePoints: StateFlow<Map<String, LiveCrewPoint>> = _livePoints.asStateFlow()
  private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
  private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
  private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()
  private val _isCrewAvailable = MutableStateFlow(true)
  val isCrewAvailable: StateFlow<Boolean> = _isCrewAvailable.asStateFlow()
  private val _incomingShootRequests = MutableStateFlow<List<Booking>>(emptyList())
  val incomingShootRequests: StateFlow<List<Booking>> = _incomingShootRequests.asStateFlow()
  /** Full-screen incoming request popup (crew): newest unhandled paid request. */
  private val _incomingAlert = MutableStateFlow<Booking?>(null)
  val incomingAlert: StateFlow<Booking?> = _incomingAlert.asStateFlow()
  /** Chat screen currently open (if any): refreshed on every push event. */
  private val _activeChatId = MutableStateFlow<String?>(null)
  private val seenSearchingIds = mutableSetOf<String>()
  private val declinedRequestIds = MutableStateFlow<Set<String>>(emptySet())
  private val _crewApplications = MutableStateFlow<List<CrewApplication>>(emptyList())
  val crewApplications: StateFlow<List<CrewApplication>> = _crewApplications.asStateFlow()
  private val seenApplicationIds = mutableSetOf<String>()

  fun setCurrentUser(user: User) {
    val previousId = _currentUser.value.id
    _currentUser.value = user
    _activeRole.value = user.role
    // Persist the profile: this is what keeps the user signed in across restarts.
    SupabaseSession.saveProfile(user)
    if (previousId.isNotBlank() && previousId != user.id) clearLocalState()
    else declinedRequestIds.value = emptySet()
    if (user.id.isNotBlank() && SupabaseConfig.isConfigured) {
      // Famebook-style live pipeline: bell + lists refresh on server events.
      SupabaseRealtimeClient.subscribeToNotifications(user.id) { onPushEvent() }
      SupabaseRealtimeClient.subscribeToBookings("user_${user.id}") { onPushEvent() }
    }
    ioScope.launch { refreshFromSupabase(user) }
  }

  /** Debounced server re-sync for realtime push events (bell + lists). */
  private var pushSyncJob: Job? = null
  fun onPushEvent() {    val user = _currentUser.value
    if (user.id.isBlank() || !SupabaseConfig.isConfigured) return
    pushSyncJob?.cancel()
    pushSyncJob = ioScope.launch {
      delay(800)
      // Drop stale events: a logout/switch during the debounce must not
      // refill the cleared state with the previous user's data.
      if (_currentUser.value.id == user.id) {
        refreshFromSupabase(user)
        // An open chat reloads on ANY push (bell, booking flip): the dedicated
        // chat socket may be dead while this one lives.
        _activeChatId.value?.let { loadChatMessages(it) }
      }
    }
  }

  /**
   * Manual pull-to-refresh target: immediate server re-sync for the signed-in
   * user. Always suspends briefly so the refresh indicator reads honestly.
   */
  suspend fun refreshNow() {
    val user = _currentUser.value
    if (user.id.isBlank() || !SupabaseConfig.isConfigured) {
      delay(600)
      return
    }
    refreshFromSupabase(user)
  }

  /**
   * Cross-device fan-out (Famebook pattern): writes a notifications row for
   * every other participant of the booking. Requires supabase/
   * 002_notification_fanout.sql to be run once; until then the insert is
   * denied and only the local bell updates.
   */  private suspend fun resolveBookingPeers(bookingId: String): Pair<String?, List<String>> {
    var clientId: String? = null
    val crewUserIds = mutableListOf<String>()
    SupabaseRestClient.get("bookings?select=client_id&id=eq.$bookingId").onSuccess { raw ->
      clientId = runCatching { JSONArray(raw).optJSONObject(0)?.optString("client_id") }
        .getOrNull()?.ifBlank { null }
    }
    SupabaseRestClient.get("booking_assignments?select=crew_id&booking_id=eq.$bookingId").onSuccess { raw ->
      val crewIds = runCatching {
        val a = JSONArray(raw)
        List(a.length()) { a.getJSONObject(it).optString("crew_id") }.filter { it.isNotBlank() }
      }.getOrDefault(emptyList())
      if (crewIds.isNotEmpty()) {
        SupabaseRestClient.get("crew_profiles?select=user_id&id=in.(${crewIds.joinToString(",")})")
          .onSuccess { raw2 ->
            runCatching {
              val a2 = JSONArray(raw2)
              for (i in 0 until a2.length()) {
                a2.getJSONObject(i).optString("user_id").ifBlank { null }?.let { crewUserIds += it }
              }
            }
          }
      }
    }
    return clientId to crewUserIds
  }

  private fun fanoutToPeers(bookingId: String, title: String, message: String) {
    val me = _currentUser.value.id
    if (!SupabaseConfig.isConfigured) return
    ioScope.launch {
      val (clientId, crewIds) = resolveBookingPeers(bookingId)
      (listOfNotNull(clientId) + crewIds).distinct()
        .filter { it.isNotBlank() && it != me }
        .forEach { target ->
          SupabaseRestClient.post(
            "notifications",
            JSONObject().apply {
              put("target_user_id", target)
              put("title", title)
              put("message", message)
              put("booking_id", bookingId)
            }.toString()
          ).onFailure {
            android.util.Log.w("FameGoPush", "fan-out denied (run 002_notification_fanout.sql): ${it.message}")
          }
        }
    }
  }
  /** Drops all cached per-user state so a logout/login never leaks data. */
  fun clearLocalState() {
    _bookings.value = emptyList()
    _notifications.value = emptyList()
    _chatMessages.value = emptyMap()
    _ratings.value = emptyMap()
    _liveSharing.value = emptySet()
    _livePoints.value = emptyMap()
    _incomingShootRequests.value = emptyList()
    _crewApplications.value = emptyList()
    seenApplicationIds.clear()
    seenSearchingIds.clear()
    _incomingAlert.value = null
    _activeSearchingBookingId.value = null
    declinedRequestIds.value = emptySet()
    _isCrewAvailable.value = true
    _allUsers.value = emptyList()
    _supportThread.value = emptyList()
    _allSupport.value = emptyList()
  }

  fun logout() {
    SupabaseSession.clear()
    SupabaseRealtimeClient.closeAll()
    FameGoPush.unregisterToken()
    _currentUser.value = User(id = "", name = "", email = "", phone = "")
    _activeRole.value = Role.CLIENT
    clearLocalState()
  }

  suspend fun restoreSignedInUser(): User? {
    val auth = SupabaseAuthClient.restoreSession().getOrNull()
    if (auth != null) {
      val profileRaw = SupabaseRestClient.get("profiles?select=*&id=eq.${auth.id}").getOrNull()
      val profile = profileRaw?.let { runCatching { JSONArray(it).optJSONObject(0) }.getOrNull() }
      if (profile != null) {
        val name = profile.optString("full_name").ifBlank { auth.email.substringBefore('@').ifBlank { "User" } }
        val role = runCatching { Role.valueOf(profile.optString("role")) }.getOrDefault(Role.CLIENT)
        return User(
          id = auth.id,
          name = name,
          email = auth.email,
          phone = profile.optString("phone"),
          companyName = profile.optString("company_name"),
          role = role,
          avatarInitials = profile.optString("avatar_initials").ifBlank { "FG" }
        ).also(::setCurrentUser)
      }
    }
    // Fully online: no cached-profile fallback. Without a live session the
    // user lands on Welcome and the No-Internet screen explains why.
    return null
  }
  fun dismissIncomingAlert() {
    _incomingAlert.value?.let { seenSearchingIds += it.id }
    _incomingAlert.value = null
  }
  fun setActiveSearchingBooking(bookingId: String?) { _activeSearchingBookingId.value = bookingId }

  fun ratingFor(bookingId: String, crewId: String): CrewRating? =
    _ratings.value["$bookingId:$crewId"]

  fun submitRating(bookingId: String, crewId: String, stars: Int, review: String) {
    if (bookingId.isBlank() || crewId.isBlank()) return
    val rating = CrewRating(bookingId, crewId, stars.coerceIn(1, 5), review.trim())
    _ratings.value = _ratings.value + ("$bookingId:$crewId" to rating)
    addNotification(
      NotificationItem(
        title = "Thanks for rating",
        message = "You rated your crew ${rating.stars} out of 5.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
    val user = _currentUser.value
    if (user.id.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch {
        runCatching {
          SupabaseRestClient.post("crew_ratings", JSONObject().apply {
            put("booking_id", bookingId)
            put("client_id", user.id)
            put("crew_id", crewId)
            put("stars", rating.stars)
            put("review", rating.review)
          }.toString())
        }
      }
    }
  }

  fun setLiveSharing(bookingId: String, enabled: Boolean) {
    if (bookingId.isBlank()) return
    if (enabled) _liveSharing.value = _liveSharing.value + bookingId
    else _liveSharing.value = _liveSharing.value - bookingId
    val crewId = _crewProfiles.value.firstOrNull { it.userId == _currentUser.value.id }?.id ?: return
    if (SupabaseConfig.isConfigured) ioScope.launch {
      SupabaseRestClient.patch(
        "crew_live_locations?crew_id=eq.$crewId&booking_id=eq.$bookingId",
        JSONObject().put("sharing_enabled", enabled).toString()
      )
    }
  }

  /** Called by the future location collector; location values always come from the device. */
  fun updateCrewLocation(bookingId: String, latitude: Double, longitude: Double) {
    val crewId = _crewProfiles.value.firstOrNull { it.userId == _currentUser.value.id }?.id ?: return
    if (!SupabaseConfig.isConfigured) return
    ioScope.launch {
      SupabaseRestClient.upsert("crew_live_locations?on_conflict=crew_id", JSONObject().apply {
        put("crew_id", crewId); put("booking_id", bookingId)
        put("latitude", latitude); put("longitude", longitude); put("sharing_enabled", true)
      }.toString())
    }
  }

  fun markChatRead(bookingId: String) {
    val current = _chatMessages.value[bookingId] ?: return
    if (current.none { !it.isFromMe && !it.isRead }) return
    _chatMessages.value = _chatMessages.value + (bookingId to current.map {
      if (!it.isFromMe) it.copy(isRead = true) else it
    })
    if (SupabaseConfig.isConfigured) {
      // Capture the sender up-front: a logout/switch between enqueue and
      // execution must not patch with the next user's id.
      val me = _currentUser.value.id
      ioScope.launch {
        runCatching {
          val stamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
          }.format(java.util.Date())
          val filter = buildString {
            append("chat_messages?booking_id=eq.$bookingId&read_at=is.null")
            if (me.isNotBlank()) append("&sender_id=neq.$me")
          }
          SupabaseRestClient.patch(filter, "{\"read_at\":\"$stamp\"}")
        }
      }
    }
  }

  /** Admin approves/rejects a shoot-crew application. */
  fun reviewCrewApplication(applicationId: String, approve: Boolean) {
    val target = _crewApplications.value.firstOrNull { it.id == applicationId } ?: return
    val next = if (approve) CrewApplicationStatus.APPROVED else CrewApplicationStatus.REJECTED
    _crewApplications.value = _crewApplications.value.map {
      if (it.id == applicationId) it.copy(status = next) else it
    }
    addNotification(
      NotificationItem(
        title = if (approve) "Shooter approved" else "Application rejected",
        message = "${target.fullName} (${target.city}) was ${next.name.lowercase().replace('_', ' ')}.",
        timestampText = "Just now",
        targetRole = Role.ADMIN,
        bookingId = null
      )
    )
    ioScope.launch {
      SupabaseRestClient.patch(
        "crew_applications?id=eq.$applicationId",
        "{\"status\":\"${next.name}\"}"
      )
    }
  }

  /** Loads pending shoot-crew applications and pings the admin phone. */
  private suspend fun refreshCrewApplications() {
    SupabaseRestClient.get("crew_applications?select=*&order=created_at.desc&limit=50")
      .onSuccess { raw ->
        val loaded = runCatching { parseCrewApplications(JSONArray(raw)) }.getOrDefault(emptyList())
        val fresh = loaded.filter {
          it.status == CrewApplicationStatus.UNDER_REVIEW && it.id !in seenApplicationIds
        }
        // First load seeds silently; only genuinely new arrivals notify.
        if (seenApplicationIds.isNotEmpty()) {
          fresh.forEach { app ->
            addNotification(
              NotificationItem(
                title = "New crew application",
                message = "${app.fullName} • ${app.city} • ${app.experienceYears} yrs. Tap to review.",
                timestampText = "Just now",
                targetRole = Role.ADMIN,
                bookingId = null
              )
            )
          }
        }
        seenApplicationIds += loaded.map { it.id }
        _crewApplications.value = loaded
      }
  }
  /** Unpaid copy of a finished shoot that reuses its details (crew re-matched after payment). */
  fun prepareRebooking(bookingId: String): Booking? {
    val src = _bookings.value.firstOrNull { it.id == bookingId } ?: return null
    return src.copy(
      id = UUID.randomUUID().toString(),
      bookingCode = "FG-" + (1000..9999).random(),
      status = BookingStatus.SEARCHING_CREW,
      paymentStatus = PaymentStatus.PENDING,
      paymentReference = "",
      assignedCrew = emptyList(),
      createdAtMillis = System.currentTimeMillis()
    )
  }

  fun completeShoot(bookingId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId) it.copy(status = BookingStatus.COMPLETED) else it }
    _liveSharing.value = _liveSharing.value - bookingId
    refreshIncomingRequests()
    addNotification(
      NotificationItem(
        title = "Shoot completed",
        message = "Your shoot has wrapped. Rate your crew and book them again.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
    ioScope.launch { SupabaseRestClient.patch("bookings?id=eq.$bookingId", "{\"status\":\"COMPLETED\"}")
      .onSuccess {
        fanoutToPeers(bookingId, "Shoot completed", "Your shoot has wrapped. Rate your crew!")
      }
    }
  }

  fun toggleCrewAvailability(crewId: String? = null) {
    val profiles = _crewProfiles.value
    val targetId = crewId ?: profiles.firstOrNull { it.userId == _currentUser.value.id }?.id
    if (targetId == null) {
      // No server profile yet (offline/demo): just flip the local capsule.
      _isCrewAvailable.value = !_isCrewAvailable.value
      return
    }
    val next = !(profiles.firstOrNull { it.id == targetId }?.isAvailable ?: _isCrewAvailable.value)
    _isCrewAvailable.value = next
    _crewProfiles.value = profiles.map { if (it.id == targetId) it.copy(isAvailable = next) else it }
    // Persist server-side so the dispatch service (and other devices) see the
    // real duty state even when this process is dead.
    if (SupabaseConfig.isConfigured) {
      ioScope.launch {
        runCatching {
          SupabaseRestClient.patch(
            "crew_profiles?id=eq.$targetId",
            JSONObject().put("is_available", next).toString()
          )
        }
      }
    }
  }

  fun createBooking(newBooking: Booking): Booking {
    // Keep the local booking even if the network sync fails (offline-first).
    val withPlan = newBooking.copy(priceRupees = newBooking.plan.priceRupees)
    _bookings.value = listOf(withPlan) + _bookings.value
    _activeSearchingBookingId.value = withPlan.id
    refreshIncomingRequests()
    addNotification(
      NotificationItem(
        title = "Finding your crew",
        message = "${withPlan.shootTitle} • ${withPlan.venueName}. We'll notify you as soon as crew responds.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = withPlan.id
      )
    )
    val user = _currentUser.value
    // Local-first: never roll back the booking if the network sync fails.
    // The UI must keep working offline and reconcile on next refresh.
    if (user.id.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch {
        // Reuse the canonical payload so plan/price/payment stay consistent
        // with the paid-booking path.
        postBookingResilient(bookingPayload(withPlan, user.id), "select=id,booking_code")
      }
    }
    return withPlan
  }

  /** Creates the authoritative paid booking before exposing it to crew search. */
  suspend fun createPaidBooking(booking: Booking): Result<Booking> {
    val user = _currentUser.value
    if (user.id.isBlank()) return Result.failure(IllegalStateException("Your session has expired"))
    if (!SupabaseConfig.isConfigured) return Result.failure(IllegalStateException("Service unavailable"))
    val payload = bookingPayload(booking, user.id)
    return postBookingResilient(payload, "select=*,booking_assignments(*)")
      .mapCatching { raw ->
        val rows = parseBookings(JSONArray(raw))
        rows.firstOrNull() ?: error("Booking was not returned by the server")
      }
      .onSuccess { saved ->
        _bookings.value = listOf(saved) + _bookings.value.filterNot { it.id == saved.id }
        _activeSearchingBookingId.value = saved.id
        refreshIncomingRequests()
        addNotification(
          NotificationItem(
            title = "Payment confirmed",
            message = "${saved.shootTitle} is ready for crew matching.",
            timestampText = "Just now",
            targetRole = Role.CLIENT,
            bookingId = saved.id
          )
        )
      }
  }

  private fun bookingPayload(booking: Booking, clientId: String) = JSONObject().apply {
    put("client_id", clientId)
    put("shoot_title", booking.shootTitle)
    put("category", booking.category.name)
    put("shoot_date", toSupabaseDate(booking.dateText))
    put("shoot_time", toSupabaseTime(booking.timeText))
    put("duration_hours", booking.durationHours)
    put("venue_name", booking.venueName)
    put("full_address", booking.fullAddress)
    put("location_instructions", booking.locationInstructions)
    put("crew_requirements", JSONArray(booking.crewRequirements.map { requirement ->
      JSONObject().apply { put("role", requirement.role.name); put("quantity", requirement.quantity) }
    }))
    put("shoot_description", booking.shootDescription)
    put("special_instructions", booking.specialInstructions)
    put("brand_name", booking.brandName)
    put("reference_link", booking.referenceLink)
    put("plan_code", booking.plan.name)
    put("plan_name", booking.plan.title)
    put("plan_price_paise", booking.priceRupees * 100)
    put("payment_status", booking.paymentStatus.name)
    put("payment_reference", booking.paymentReference)
  }

  /**
   * Posts a booking, surviving a stale server schema: if PostgREST rejects an
   * unknown column (PGRST204, e.g. a project that hasn't run the repair
   * migration), that key is stripped and the post is retried once.
   */
  private suspend fun postBookingResilient(payload: JSONObject, select: String): Result<String> {
    val first = SupabaseRestClient.post("bookings?$select", payload.toString())
    if (first.isSuccess) return first
    val missing = Regex("Could not find the '([^']+)' column")
      .find(first.exceptionOrNull()?.message.orEmpty())?.groupValues?.getOrNull(1)
    if (missing != null && payload.has(missing)) {
      payload.remove(missing)
      android.util.Log.w("FameGoRepo", "server lacks column $missing — retried without it")
      return SupabaseRestClient.post("bookings?$select", payload.toString())
    }
    return first
  }

  fun friendlyMessage(error: Throwable): String {
    val raw = error.message.orEmpty()
    val message = raw
    return when {
      SupabaseNetwork.isNetworkFailure(error) -> "Please check your internet connection and try again."
      message.contains("401") || message.contains("session", true) -> "Your session expired. Please sign in again."
      message.startsWith("Supabase ") -> "Server: ${message.removePrefix("Supabase ").take(220)}"
      else -> "Unable to complete this action. Please try again."
    }
  }

  fun crewAcceptBooking(bookingId: String, crewId: String) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW || booking.assignedCrew.any { it.crewId == crewId }) return
    // Resolve a full member from cached profiles, but never fail when the
    // profile list is empty (offline/demo): fall back to a verified member.
    val member = _crewProfiles.value.firstOrNull { it.id == crewId }?.toAssignedMember()
      ?: _crewProfiles.value.firstOrNull()?.toAssignedMember()?.copy(crewId = crewId)
      ?: AssignedCrewMember(
        crewId = crewId.ifBlank { "crew_demo_1" },
        name = "Aarav Mehta",
        role = com.example.model.CrewRoleType.CINEMATOGRAPHER,
        phone = "+91 98200 11223",
        gear = "Sony FX6 Cinema Line & Rig",
        rating = 4.95,
        isVerified = true
      )
    applyCrewConfirmed(bookingId, member)
  }

  fun crewDeclineBooking(bookingId: String, crewId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    declinedRequestIds.value = declinedRequestIds.value + bookingId
    if (_incomingAlert.value?.id == bookingId) dismissIncomingAlert()
    refreshIncomingRequests()
    addNotification(NotificationItem(title = "Shoot request declined", message = "You declined this request. We'll keep looking for other crew.", timestampText = "Just now", targetRole = Role.CREW, bookingId = bookingId))
  }

  fun declineShootRequest(bookingId: String) = crewDeclineBooking(bookingId, _currentUser.value.id)
  fun acceptShootRequest(bookingId: String, crewMember: AssignedCrewMember) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW) return
    applyCrewConfirmed(bookingId, crewMember)
  }

  private fun applyCrewConfirmed(bookingId: String, member: AssignedCrewMember, confirmServer: Boolean = true) {
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CONFIRMED, assignedCrew = listOf(member)) else it
    }
    _crewProfiles.value = _crewProfiles.value.map { if (it.id == member.crewId) it.copy(isAvailable = false) else it }
    _isCrewAvailable.value = false
    refreshIncomingRequests()
    addNotification(
      NotificationItem(
        title = "Crew confirmed",
        message = "${member.name} (${member.role.title}) is locked in for your shoot.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = bookingId
      )
    )
    ioScope.launch {
      if (!confirmServer) {
        fanoutToPeers(bookingId, "Crew confirmed", "${member.name} is locked in for your shoot.")
        return@launch
      }
      val rpc = SupabaseRestClient.post(
        "rpc/accept_booking",
        JSONObject().apply { put("p_booking_id", bookingId) }.toString()
      )
      rpc.onSuccess {
        // The accepting crew's device tells the client's devices.
        fanoutToPeers(bookingId, "Crew confirmed", "${member.name} is locked in for your shoot.")
      }.onFailure { e ->        // Definitive rejections roll back the optimistic confirm and say why;
        // transient network errors keep it and reconcile on next refresh.
        val msg = e.message.orEmpty()
        val definitive = msg.contains("booking_not_available") ||
          msg.contains("crew_not_available") ||
          Regex("Supabase 40[013]").containsMatchIn(msg)
        if (!definitive) return@launch
        _bookings.value = _bookings.value.map {
          if (it.id == bookingId) it.copy(status = BookingStatus.SEARCHING_CREW, assignedCrew = emptyList())
          else it
        }
        refreshIncomingRequests()
        when (val verdict = AcceptResolver.resolve(bookingId)) {
          // Server already has me on this shoot: keep local truth, no RPC loop.
          is AcceptOutcome.Confirmed -> applyCrewConfirmed(bookingId, member, confirmServer = false)
          is AcceptOutcome.Taken -> addNotification(
            NotificationItem(
              title = "Already claimed",
              message = "${verdict.name} claimed this shoot first.",
              timestampText = "Just now",
              targetRole = Role.CREW,
              bookingId = bookingId
            )
          )
          AcceptOutcome.OffDuty -> addNotification(
            NotificationItem(
              title = "You're off duty",
              message = "Go on duty to claim shoots.",
              timestampText = "Just now",
              targetRole = Role.CREW,
              bookingId = bookingId
            )
          )
          AcceptOutcome.Gone -> addNotification(
            NotificationItem(
              title = "Request closed",
              message = "This shoot is no longer open.",
              timestampText = "Just now",
              targetRole = Role.CREW,
              bookingId = bookingId
            )
          )
          AcceptOutcome.Retry -> addNotification(
            NotificationItem(
              title = "Couldn't claim yet",
              message = "Check connection and retry from Requests.",
              timestampText = "Just now",
              targetRole = Role.CREW,
              bookingId = bookingId
            )
          )
        }
      }
    }
  }

  fun cancelBooking(bookingId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId && it.status != BookingStatus.COMPLETED) it.copy(status = BookingStatus.CANCELLED) else it }
    if (_activeSearchingBookingId.value == bookingId) _activeSearchingBookingId.value = null
    refreshIncomingRequests()
    ioScope.launch {
      SupabaseRestClient.patch("bookings?id=eq.$bookingId", "{\"status\":\"CANCELLED\"}")
        .onSuccess {
          fanoutToPeers(bookingId, "Booking cancelled", "A shoot booking was cancelled.")
        }
    }
  }

  fun deleteBooking(bookingId: String) {
    _bookings.value = _bookings.value.filterNot { it.id == bookingId }
    if (_activeSearchingBookingId.value == bookingId) _activeSearchingBookingId.value = null
    refreshIncomingRequests()
    ioScope.launch { SupabaseRestClient.delete("bookings?id=eq.$bookingId") }
  }

  fun adminAssignCrew(bookingId: String, crewId: String) {
    val crew = _crewProfiles.value.firstOrNull { it.id == crewId } ?: return
    assignCrewToBooking(bookingId, crew.toAssignedMember())
  }

  fun assignCrewToBooking(bookingId: String, crew: AssignedCrewMember) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId) it.copy(status = BookingStatus.CONFIRMED, assignedCrew = listOf(crew)) else it }
    refreshIncomingRequests()
  }

  fun adminUpdateBookingStatus(bookingId: String, newStatus: BookingStatus) {
    if (_bookings.value.none { it.id == bookingId }) return
    if (newStatus == BookingStatus.COMPLETED) {
      completeShoot(bookingId)
      return
    }
    _bookings.value = _bookings.value.map { if (it.id == bookingId) it.copy(status = newStatus) else it }
    refreshIncomingRequests()
  }
  fun updateBookingStatus(bookingId: String, status: BookingStatus) = adminUpdateBookingStatus(bookingId, status)
  fun adminVerifyCrew(crewId: String, status: VerificationStatus) { _crewProfiles.value = _crewProfiles.value.map { if (it.id == crewId) it.copy(verificationStatus = status) else it } }

  // -- Admin user management -------------------------------------------------
  private val _allUsers = MutableStateFlow<List<User>>(emptyList())
  val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

  /** Admin inbox: every profile, newest first. Reads live in the Users panel. */
  fun loadAllUsers() {
    if (!SupabaseConfig.isConfigured || _currentUser.value.role != Role.ADMIN) return
    ioScope.launch {
      SupabaseRestClient.get("profiles?select=*&order=created_at.desc&limit=200")
        .onSuccess { raw ->
          val loaded = runCatching { parseUsers(JSONArray(raw)) }.getOrDefault(emptyList())
          if (loaded.isNotEmpty()) _allUsers.value = loaded
        }
    }
  }

  /**
   * Switches a user's role (CLIENT / CREW / ADMIN). Promoting to CREW also
   * ensures the crew_profiles stub so the shooter can accept requests.
   * Requires supabase/007_admin_user_management.sql on the server.
   */
  suspend fun updateUserRole(userId: String, role: Role): Result<Unit> {
    if (userId.isBlank()) return Result.failure(IllegalStateException("Unknown user"))
    val previous = _allUsers.value
    _allUsers.value = previous.map { if (it.id == userId) it.copy(role = role) else it }
    val patched = SupabaseRestClient.patch(
      "profiles?id=eq.$userId",
      JSONObject().put("role", role.name).toString()
    )
    if (patched.isFailure) {
      _allUsers.value = previous
      return Result.failure(patched.exceptionOrNull() ?: IllegalStateException("Role update failed"))
    }
    if (role == Role.CREW) {
      // Best-effort stub: without it the new shooter can't go available.
      SupabaseRestClient.upsert(
        "crew_profiles?on_conflict=user_id",
        JSONObject().apply {
          put("user_id", userId)
          put("primary_role", "ASSISTANT")
          put("is_available", true)
        }.toString()
      )
    }
    return Result.success(Unit)
  }

  fun sendChatMessage(bookingId: String, text: String, senderRole: Role, senderName: String) {
    val message = text.trim()
    if (message.isEmpty() || bookingId.isBlank()) return
    val resolvedName = senderName.ifBlank { _currentUser.value.name.ifBlank { "You" } }
    val updated = (_chatMessages.value[bookingId] ?: emptyList()) + ChatMessage(bookingId = bookingId, senderName = resolvedName, senderRole = senderRole, message = message, timeText = "Just now", isFromMe = true)
    _chatMessages.value = _chatMessages.value + (bookingId to updated)
    val senderId = _currentUser.value.id
    if (senderId.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch {
        SupabaseRestClient.post("chat_messages", JSONObject().apply {
          put("booking_id", bookingId)
          put("sender_id", senderId)
          put("message", message)
        }.toString()).onSuccess {
          fanoutToPeers(bookingId, "New message", "$resolvedName: ${message.take(120)}")
        }
      }
    }
  }

  fun submitSupportMessage(text: String): Boolean {
    val message = text.trim()
    if (message.isEmpty()) return false
    val userId = _currentUser.value.id
    if (userId.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch {
        SupabaseRestClient.post("support_messages", JSONObject().apply {
          put("user_id", userId)
          put("message", message)
        }.toString()).onSuccess { loadSupportThread() }
      }
    }
    return true
  }

  // -- Support threads (two-way) -------------------------------------------
  private val _supportThread = MutableStateFlow<List<com.example.model.SupportMessage>>(emptyList())
  val supportThread: StateFlow<List<com.example.model.SupportMessage>> = _supportThread.asStateFlow()
  private val _allSupport = MutableStateFlow<List<com.example.model.SupportMessage>>(emptyList())
  val allSupport: StateFlow<List<com.example.model.SupportMessage>> = _allSupport.asStateFlow()

  /** Own conversation (mine + admin replies). */
  fun loadSupportThread() {
    val userId = _currentUser.value.id
    if (userId.isBlank() || !SupabaseConfig.isConfigured) return
    ioScope.launch {
      SupabaseRestClient.get("support_messages?select=*&user_id=eq.$userId&order=created_at.asc&limit=100")
        .onSuccess { raw ->
          _supportThread.value = runCatching { parseSupport(JSONArray(raw)) }.getOrDefault(emptyList())
        }
    }
  }

  /** Admin inbox: every thread, newest first. Requires 008_support_replies.sql. */
  fun loadAllSupport() {
    if (!SupabaseConfig.isConfigured || _currentUser.value.role != Role.ADMIN) return
    ioScope.launch {
      SupabaseRestClient.get("support_messages?select=*&order=created_at.desc&limit=200")
        .onSuccess { raw ->
          _allSupport.value = runCatching { parseSupport(JSONArray(raw)) }.getOrDefault(emptyList())
        }
    }
  }

  /** Admin reply inside a user's thread. */
  suspend fun replySupport(targetUserId: String, text: String): Result<Unit> {
    val message = text.trim()
    if (message.isEmpty() || targetUserId.isBlank()) {
      return Result.failure(IllegalStateException("Write a reply first"))
    }
    return SupabaseRestClient.post("support_messages", JSONObject().apply {
      put("user_id", targetUserId)
      put("message", message)
      put("is_from_support", true)
    }.toString()).map { loadAllSupport() }
  }

  private fun parseSupport(array: JSONArray): List<com.example.model.SupportMessage> = buildList {
    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      if (o.optString("message").isBlank()) continue
      add(
        com.example.model.SupportMessage(
          id = o.optString("id"),
          userId = o.optString("user_id"),
          message = o.optString("message"),
          isFromSupport = o.optBoolean("is_from_support"),
          createdAt = o.optString("created_at")
        )
      )
    }
  }

  /**
   * Sends a shoot-crew application. Works logged-out (anon insert) — this is
   * how under-review applicants reach you. Reads live in Supabase Dashboard >
   * Table Editor > crew_applications.
   */
  suspend fun submitCrewApplication(
    fullName: String,
    phone: String,
    email: String,
    city: String,
    iphoneModel: String,
    portfolioUrl: String,
    instagram: String,
    experienceYears: Int,
    bestShoot: String
  ): Result<Unit> {
    if (!SupabaseConfig.isConfigured) {
      return Result.failure(IllegalStateException("Service unavailable. Check connection and retry."))
    }
    val payload = JSONObject().apply {
      put("full_name", fullName.trim())
      put("phone", phone.trim())
      put("email", email.trim())
      put("city", city.trim())
      put("iphone_model", iphoneModel.trim())
      put("gear_summary", "iPhone 14 Pro+ / Gimbal / Wireless mic / LED light / Power bank — confirmed by applicant")
      put("portfolio_url", portfolioUrl.trim())
      put("instagram_handle", instagram.trim())
      put("experience_years", experienceYears.coerceAtLeast(0))
      put("best_shoot", bestShoot.trim())
    }.toString()
    return SupabaseRestClient.post("crew_applications", payload).map { }
  }

  fun addNotification(item: NotificationItem) { _notifications.value = listOf(item) + _notifications.value }
  fun loadChatMessages(bookingId: String) {
    if (!SupabaseConfig.isConfigured) return
    ioScope.launch {
      SupabaseRestClient.get("chat_messages?select=*&booking_id=eq.$bookingId&order=created_at.asc")
        .onSuccess { raw ->
          val userId = _currentUser.value.id
          val messages = runCatching {
            val array = JSONArray(raw)
            buildList {
              for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val isMine = o.optString("sender_id") == userId
                val senderRole = if (isMine) _currentUser.value.role else Role.CREW
                val senderName = o.optString("sender_name").ifBlank {
                  if (isMine) _currentUser.value.name.ifBlank { "You" } else "Crew"
                }
                add(ChatMessage(o.optString("id"), bookingId, senderName, senderRole, o.optString("message"), o.optString("created_at").take(16).replace("T", " "), isMine, o.optString("read_at").isNotBlank()))
              }
            }
          }.getOrDefault(emptyList())
          if (messages.isNotEmpty()) {
            // Merge: keep optimistic local messages that the server hasn't echoed yet.
            // Match by stable id first; fall back to text match for just-sent rows
            // that have no server id yet.
            val remoteIds = messages.map { it.id }.toSet()
            val localOnly = (_chatMessages.value[bookingId] ?: emptyList()).filter { local ->
              local.id !in remoteIds && messages.none { remote ->
                remote.message == local.message && remote.isFromMe == local.isFromMe
              }
            }
            _chatMessages.value = _chatMessages.value + (bookingId to (messages + localOnly))
          }
        }
    }
  }

  fun startChatRealtime(bookingId: String) {
    _activeChatId.value = bookingId
    SupabaseRealtimeClient.subscribeToChat(bookingId) { loadChatMessages(bookingId) }
  }

  fun stopChatRealtime(bookingId: String) {
    if (_activeChatId.value == bookingId) _activeChatId.value = null
    SupabaseRealtimeClient.unsubscribeFromChat(bookingId)
  }
  private fun refreshIncomingRequests() {
    _incomingShootRequests.value = _bookings.value.filter {
      it.status == BookingStatus.SEARCHING_CREW && it.id !in declinedRequestIds.value
    }
  }
  fun markAllNotificationsRead(role: Role? = null) {
    _notifications.value = _notifications.value.map { if (role == null || it.targetRole == role) it.copy(isRead = true) else it }
    val userId = _currentUser.value.id
    if (userId.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch { SupabaseRestClient.patch("notifications?target_user_id=eq.$userId&is_read=eq.false", "{\"is_read\":true}") }
    }
  }

  fun markNotificationRead(id: String) {
    _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
    if (SupabaseConfig.isConfigured) {
      ioScope.launch { SupabaseRestClient.patch("notifications?id=eq.$id", "{\"is_read\":true}") }
    }
  }

  private fun CrewProfile.toAssignedMember() = AssignedCrewMember(id, fullName, primaryRole, phone, gearSummary, rating, verificationStatus == VerificationStatus.VERIFIED)

  private suspend fun refreshFromSupabase(user: User) {
    if (user.id.isBlank() || !SupabaseConfig.isConfigured) return
    SupabaseRestClient.get("bookings?select=*,booking_assignments(*)&client_id=eq.${user.id}&order=created_at.desc")
      .onSuccess { raw ->
        val loaded = runCatching { parseBookings(JSONArray(raw)) }.getOrDefault(emptyList())
        // Preserve optimistic local bookings created while offline.
        val localOnly = _bookings.value.filter { local -> loaded.none { it.id == local.id } && local.status == BookingStatus.SEARCHING_CREW }
        _bookings.value = loaded + localOnly
        refreshIncomingRequests()
      }
    // Crew + admin need the open pool so shoot requests actually appear.
    if (user.role != Role.CLIENT) {
      SupabaseRestClient.get("bookings?select=*,booking_assignments(*)&status=eq.SEARCHING_CREW&order=created_at.desc&limit=25")
        .onSuccess { raw ->
          val open = runCatching { parseBookings(JSONArray(raw)) }.getOrDefault(emptyList())
          // Famebook-style crew alert: first load seeds silently, genuinely new
          // paid requests pop the full-screen incoming alert + local bell.
          if (user.role == Role.CREW) {
            val fresh = open.filter {
              it.id !in seenSearchingIds && it.id !in declinedRequestIds.value
            }
            if (seenSearchingIds.isNotEmpty()) {
              fresh.firstOrNull()?.let { newest ->
                _incomingAlert.value = newest
                addNotification(
                  NotificationItem(
                    title = "New shoot request",
                    message = "${newest.shootTitle} • ${newest.venueName} • ₹${newest.priceRupees}. Tap to review.",
                    timestampText = "Just now",
                    targetRole = Role.CREW,
                    bookingId = newest.id
                  )
                )
              }
            }
            seenSearchingIds += open.map { it.id }
          }
          val merged = (_bookings.value + open).distinctBy { it.id }
          _bookings.value = merged
          refreshIncomingRequests()
        }
    }
    SupabaseRestClient.get("notifications?select=*&target_user_id=eq.${user.id}&order=created_at.desc")
      .onSuccess { raw -> _notifications.value = runCatching { parseNotifications(JSONArray(raw), user.role) }.getOrDefault(emptyList()) }
    SupabaseRestClient.get("crew_ratings?select=*&client_id=eq.${user.id}")
      .onSuccess { raw ->
        _ratings.value = runCatching {
          val array = JSONArray(raw)
          buildMap {
            for (i in 0 until array.length()) {
              val o = array.getJSONObject(i)
              val rating = CrewRating(
                o.optString("booking_id"), o.optString("crew_id"),
                o.optInt("stars", 5).coerceIn(1, 5), o.optString("review")
              )
              put("${rating.bookingId}:${rating.crewId}", rating)
            }
          }
        }.getOrDefault(emptyMap())
      }
    val bookingIds = _bookings.value.map { it.id }.filter { it.isNotBlank() }
    if (bookingIds.isNotEmpty()) {
      val filter = bookingIds.joinToString(",", prefix = "(", postfix = ")")
      SupabaseRestClient.get("crew_live_locations?select=*&booking_id=in.$filter&sharing_enabled=eq.true")
        .onSuccess { raw ->
          runCatching {
            val array = JSONArray(raw)
            val points = buildMap {
              for (i in 0 until array.length()) {
                val point = array.getJSONObject(i)
                put(point.optString("booking_id"), LiveCrewPoint(
                  point.optDouble("latitude"), point.optDouble("longitude"), "Updated just now"
                ))
              }
            }
            _livePoints.value = points
            _liveSharing.value = points.keys
          }
        }
    }
    SupabaseRestClient.get("crew_profiles?select=*&order=rating.desc")
      .onSuccess { raw -> _crewProfiles.value = runCatching { parseCrewProfiles(JSONArray(raw)) }.getOrDefault(emptyList()) }
    // Admins pull the crew-application inbox so new forms ping their phone.
    if (user.role == Role.ADMIN) refreshCrewApplications()
  }

  /** UI uses friendly labels ("Today", "09:00 AM"); Supabase needs ISO date/time. */
  fun toSupabaseDate(display: String): String {
    val trimmed = display.trim()
    // ISO dates ("2026-09-15") pass straight through — never run them through
    // the day-chip heuristic (digit filter would build a huge bogus day).
    val iso = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})$""").matchEntire(trimmed)
    if (iso != null) {
      val (year, month, day) = iso.destructured
      val y = year.toIntOrNull() ?: return todayIso()
      val m = month.toIntOrNull() ?: return todayIso()
      val d = day.toIntOrNull() ?: return todayIso()
      if (y in 2020..2100 && m in 1..12 && d in 1..31) {
        return "%04d-%02d-%02d".format(y, m, d)
      }
      return todayIso()
    }
    val cal = java.util.Calendar.getInstance()
    when (trimmed.lowercase()) {
      "today" -> Unit
      "tomorrow" -> cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
      else -> {
        // Accept "Fri 12", "Sat 13" style chips: resolve day-of-month in current month.
        val day = trimmed.filter { it.isDigit() }.toIntOrNull()
        if (day != null && day in 1..31) {
          val today = cal.get(java.util.Calendar.DAY_OF_MONTH)
          if (day >= today) cal.set(java.util.Calendar.DAY_OF_MONTH, day)
          else { cal.add(java.util.Calendar.MONTH, 1); cal.set(java.util.Calendar.DAY_OF_MONTH, day) }
        }
      }
    }
    return "%04d-%02d-%02d".format(
      cal.get(java.util.Calendar.YEAR),
      cal.get(java.util.Calendar.MONTH) + 1,
      cal.get(java.util.Calendar.DAY_OF_MONTH)
    )
  }

  private fun todayIso(): String {
    val cal = java.util.Calendar.getInstance()
    return "%04d-%02d-%02d".format(
      cal.get(java.util.Calendar.YEAR),
      cal.get(java.util.Calendar.MONTH) + 1,
      cal.get(java.util.Calendar.DAY_OF_MONTH)
    )
  }

  fun toSupabaseTime(display: String): String {
    // "09:00 AM" -> "09:00:00", "02:00 PM" -> "14:00:00". Fall back to 09:00.
    val match = Regex("""(\d{1,2}):(\d{2})\s*([AaPp][Mm])?""").find(display.trim())
    if (match != null) {
      var hour = match.groupValues[1].toIntOrNull() ?: return "09:00:00"
      val minute = match.groupValues[2].toIntOrNull() ?: return "09:00:00"
      if (hour !in 0..23 || minute !in 0..59) return "09:00:00"
      val ampm = match.groupValues[3].uppercase()
      if (ampm == "PM" && hour < 12) hour += 12
      if (ampm == "AM" && hour == 12) hour = 0
      return "%02d:%02d:00".format(hour, minute)
    }
    return display.ifBlank { "09:00:00" }
  }

  fun toDisplayTime(supabaseTime: String): String {
    val match = Regex("""(\d{1,2}):(\d{2})""").find(supabaseTime)
    if (match != null) {
      val hour24 = match.groupValues[1].toIntOrNull() ?: return supabaseTime
      val minute = match.groupValues[2]
      val ampm = if (hour24 >= 12) "PM" else "AM"
      val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
      }
      return "%02d:%s %s".format(hour12, minute, ampm)
    }
    return supabaseTime
  }

  private fun parseBookings(array: JSONArray): List<Booking> = buildList {
    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      val requirements = o.optJSONArray("crew_requirements") ?: JSONArray()
      val parsedRequirements = buildList {
        for (j in 0 until requirements.length()) {
          val r = requirements.getJSONObject(j)
          val role = runCatching { com.example.model.CrewRoleType.valueOf(r.optString("role")) }.getOrNull() ?: continue
          add(com.example.model.CrewRequirement(role, r.optInt("quantity", 1).coerceAtLeast(1)))
        }
      }
      val category = runCatching { com.example.model.ShootCategory.valueOf(o.optString("category")) }.getOrDefault(com.example.model.ShootCategory.VIDEO)
      val status = runCatching { BookingStatus.valueOf(o.optString("status")) }.getOrDefault(BookingStatus.SEARCHING_CREW)
      val plan = runCatching { ShootPlan.valueOf(o.optString("plan_code")) }.getOrDefault(ShootPlan.BRONZE_3H)
      val payment = runCatching { PaymentStatus.valueOf(o.optString("payment_status")) }.getOrDefault(PaymentStatus.PENDING)
      val assignments = o.optJSONArray("booking_assignments") ?: JSONArray()
      val assignedCrew = buildList {
        for (j in 0 until assignments.length()) {
          val assignment = assignments.getJSONObject(j)
          val role = runCatching { com.example.model.CrewRoleType.valueOf(assignment.optString("role")) }
            .getOrDefault(com.example.model.CrewRoleType.ASSISTANT)
          add(AssignedCrewMember(
            crewId = assignment.optString("crew_id"),
            name = assignment.optString("name_snapshot").ifBlank { "FameGo Crew" },
            role = role,
            phone = assignment.optString("phone_snapshot"),
            gear = assignment.optString("gear_snapshot"),
            rating = assignment.optDouble("rating_snapshot", 0.0),
            isVerified = assignment.optBoolean("is_verified_snapshot")
          ))
        }
      }
      add(Booking(
        id = o.optString("id"), bookingCode = o.optString("booking_code").ifBlank { "FG-0000" }, shootTitle = o.optString("shoot_title").ifBlank { "Untitled shoot" },
        category = category, dateText = o.optString("shoot_date").ifBlank { "Today" }, timeText = toDisplayTime(o.optString("shoot_time").ifBlank { "09:00 AM" }),
        durationHours = o.optInt("duration_hours", 2).coerceIn(1, 24), venueName = o.optString("venue_name"), fullAddress = o.optString("full_address"),
        locationInstructions = o.optString("location_instructions"), crewRequirements = parsedRequirements,
        shootDescription = o.optString("shoot_description"), specialInstructions = o.optString("special_instructions"),
        brandName = o.optString("brand_name"), referenceLink = o.optString("reference_link"),
        plan = plan, priceRupees = (o.optInt("plan_price_paise", plan.priceRupees * 100) / 100).coerceAtLeast(0),
        paymentStatus = payment, paymentReference = o.optString("payment_reference"),
        status = status, assignedCrew = assignedCrew
      ))
    }
  }

  private fun parseUsers(array: JSONArray): List<User> = buildList {
    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      val id = o.optString("id")
      if (id.isBlank()) continue
      val name = o.optString("full_name").ifBlank {
        o.optString("email").substringBefore('@').ifBlank { "User" }
      }
      val role = runCatching { Role.valueOf(o.optString("role")) }.getOrDefault(Role.CLIENT)
      add(
        User(
          id = id,
          name = name,
          email = o.optString("email"),
          phone = o.optString("phone"),
          companyName = o.optString("company_name"),
          role = role,
          avatarInitials = o.optString("avatar_initials").ifBlank {
            name.split(" ").filter { it.isNotBlank() }.take(2)
              .joinToString("") { it.first().uppercase() }.ifEmpty { "FG" }
          }
        )
      )
    }
  }

  private fun parseNotifications(array: JSONArray, role: Role): List<NotificationItem> = buildList {    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      add(NotificationItem(o.optString("id"), o.optString("title"), o.optString("message"), o.optString("created_at"), role, o.optBoolean("is_read"), o.optString("booking_id").ifBlank { null }))
    }
  }

  private fun parseCrewApplications(array: JSONArray): List<CrewApplication> = buildList {
    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      val status = runCatching { CrewApplicationStatus.valueOf(o.optString("status")) }
        .getOrDefault(CrewApplicationStatus.UNDER_REVIEW)
      add(
        CrewApplication(
          id = o.optString("id"),
          fullName = o.optString("full_name"),
          phone = o.optString("phone"),
          email = o.optString("email"),
          city = o.optString("city"),
          iphoneModel = o.optString("iphone_model"),
          gearSummary = o.optString("gear_summary"),
          portfolioUrl = o.optString("portfolio_url"),
          instagramHandle = o.optString("instagram_handle"),
          experienceYears = o.optInt("experience_years"),
          bestShoot = o.optString("best_shoot"),
          status = status,
          createdAt = o.optString("created_at")
        )
      )
    }
  }

  private fun parseCrewProfiles(array: JSONArray): List<CrewProfile> = buildList {    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      val role = runCatching { com.example.model.CrewRoleType.valueOf(o.optString("primary_role")) }.getOrDefault(com.example.model.CrewRoleType.ASSISTANT)
      val verification = runCatching { VerificationStatus.valueOf(o.optString("verification_status")) }.getOrDefault(VerificationStatus.PENDING_VERIFICATION)
      // crew_profiles has no name columns; names may arrive via join aliases.
      val fullName = o.optString("full_name").ifBlank { o.optString("display_name").ifBlank { o.optString("name") } }
      val phone = o.optString("phone")
      val email = o.optString("email")
      val initials = fullName.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "CR" }
      add(CrewProfile(o.optString("id"), o.optString("user_id"), fullName, phone, email, o.optString("city").ifBlank { "Mumbai" }, role, emptyList(), o.optInt("experience_years"), o.optString("bio"), o.optString("gear_summary"), o.optString("portfolio_url"), o.optString("instagram_handle"), verification, o.optBoolean("is_available", true), o.optDouble("rating", 4.9), o.optInt("total_shoots_completed"), initials))
    }
  }
}
