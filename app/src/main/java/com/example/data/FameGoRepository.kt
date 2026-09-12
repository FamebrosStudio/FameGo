package com.example.data

import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.ChatMessage
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
import kotlin.math.cos
import kotlin.math.sin

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
  private val _favoriteCrewIds = MutableStateFlow<Set<String>>(emptySet())
  val favoriteCrewIds: StateFlow<Set<String>> = _favoriteCrewIds.asStateFlow()
  private val _ratings = MutableStateFlow<Map<String, CrewRating>>(emptyMap())
  val ratings: StateFlow<Map<String, CrewRating>> = _ratings.asStateFlow()
  private val _liveSharing = MutableStateFlow<Set<String>>(emptySet())
  val liveSharing: StateFlow<Set<String>> = _liveSharing.asStateFlow()
  private val _livePoints = MutableStateFlow<Map<String, LiveCrewPoint>>(emptyMap())
  val livePoints: StateFlow<Map<String, LiveCrewPoint>> = _livePoints.asStateFlow()
  private val liveJobs = mutableMapOf<String, Job>()
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
  private val declinedRequestIds = MutableStateFlow<Set<String>>(emptySet())

  fun setCurrentUser(user: User) {
    _currentUser.value = user
    _activeRole.value = user.role
    declinedRequestIds.value = emptySet()
    ioScope.launch { refreshFromSupabase(user) }
  }

  suspend fun restoreSignedInUser(): User? {
    val auth = SupabaseAuthClient.restoreSession().getOrNull() ?: return null
    val profileRaw = SupabaseRestClient.get("profiles?select=*&id=eq.${auth.id}").getOrNull() ?: return null
    val profile = runCatching { JSONArray(profileRaw).optJSONObject(0) }.getOrNull() ?: return null
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
  fun switchRole(role: Role) { _activeRole.value = role; _currentUser.value = _currentUser.value.copy(role = role) }
  fun setActiveSearchingBooking(bookingId: String?) { _activeSearchingBookingId.value = bookingId }

  fun toggleFavoriteCrew(crewId: String) {
    if (crewId.isBlank()) return
    val adding = crewId !in _favoriteCrewIds.value
    _favoriteCrewIds.value = _favoriteCrewIds.value.toMutableSet().apply { if (!add(crewId)) remove(crewId) }
    val user = _currentUser.value
    if (user.id.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch {
        runCatching {
          if (adding) {
            SupabaseRestClient.post("favorite_crew", JSONObject().apply {
              put("client_id", user.id)
              put("crew_id", crewId)
            }.toString())
          } else {
            SupabaseRestClient.delete("favorite_crew?client_id=eq.${user.id}&crew_id=eq.$crewId")
          }
        }
      }
    }
  }

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

  /** Demo live tracking: emits a drifting point near the venue until stopped. */
  fun setLiveSharing(bookingId: String, enabled: Boolean) {
    if (bookingId.isBlank()) return
    if (enabled) {
      if (bookingId in _liveSharing.value) return
      _liveSharing.value = _liveSharing.value + bookingId
      var lat = 19.0596
      var lng = 72.8295
      var step = 0
      _livePoints.value = _livePoints.value + (bookingId to LiveCrewPoint(lat, lng, "Crew is nearby"))
      liveJobs[bookingId]?.cancel()
      liveJobs[bookingId] = ioScope.launch {
        while (true) {
          delay(4000)
          step++
          lat += 0.0004 * cos(step * 0.7)
          lng += 0.0004 * sin(step * 0.9)
          _livePoints.value = _livePoints.value + (bookingId to LiveCrewPoint(lat, lng, "Updated just now"))
        }
      }
    } else {
      liveJobs.remove(bookingId)?.cancel()
      _liveSharing.value = _liveSharing.value - bookingId
    }
  }

  fun markChatRead(bookingId: String) {
    val current = _chatMessages.value[bookingId] ?: return
    if (current.none { !it.isFromMe && !it.isRead }) return
    _chatMessages.value = _chatMessages.value + (bookingId to current.map {
      if (!it.isFromMe) it.copy(isRead = true) else it
    })
    if (SupabaseConfig.isConfigured) {
      ioScope.launch {
        runCatching {
          val stamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
          }.format(java.util.Date())
          SupabaseRestClient.patch(
            "chat_messages?booking_id=eq.$bookingId&read_at=is.null",
            "{\"read_at\":\"$stamp\"}"
          )
        }
      }
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
    liveJobs.remove(bookingId)?.cancel()
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
    ioScope.launch { SupabaseRestClient.patch("bookings?id=eq.$bookingId", "{\"status\":\"COMPLETED\"}") }
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
  }

  fun createBooking(newBooking: Booking): Booking {
    _bookings.value = listOf(newBooking) + _bookings.value
    _activeSearchingBookingId.value = newBooking.id
    refreshIncomingRequests()
    addNotification(
      NotificationItem(
        title = "Finding your crew",
        message = "${newBooking.shootTitle} • ${newBooking.venueName}. We'll notify you as soon as crew responds.",
        timestampText = "Just now",
        targetRole = Role.CLIENT,
        bookingId = newBooking.id
      )
    )
    val user = _currentUser.value
    // Local-first: never roll back the booking if the network sync fails.
    // The UI must keep working offline and reconcile on next refresh.
    if (user.id.isNotBlank() && SupabaseConfig.isConfigured) {
      ioScope.launch {
        val payload = JSONObject().apply {
          put("client_id", user.id)
          put("shoot_title", newBooking.shootTitle)
          put("category", newBooking.category.name)
          put("shoot_date", toSupabaseDate(newBooking.dateText))
          put("shoot_time", toSupabaseTime(newBooking.timeText))
          put("duration_hours", newBooking.durationHours)
          put("venue_name", newBooking.venueName)
          put("full_address", newBooking.fullAddress)
          put("location_instructions", newBooking.locationInstructions)
          put("crew_requirements", JSONArray(newBooking.crewRequirements.map { JSONObject().apply { put("role", it.role.name); put("quantity", it.quantity) } }))
          put("shoot_description", newBooking.shootDescription)
          put("special_instructions", newBooking.specialInstructions)
          put("brand_name", newBooking.brandName)
          put("reference_link", newBooking.referenceLink)
        }.toString()
        SupabaseRestClient.post("bookings?select=id,booking_code", payload)
      }
    }
    return newBooking
  }

  /** Creates the authoritative paid booking before exposing it to crew search. */
  suspend fun createPaidBooking(booking: Booking): Result<Booking> {
    val user = _currentUser.value
    if (user.id.isBlank()) return Result.failure(IllegalStateException("Your session has expired"))
    if (!SupabaseConfig.isConfigured) return Result.failure(IllegalStateException("Service unavailable"))
    val payload = bookingPayload(booking, user.id)
    return SupabaseRestClient.post("bookings?select=*,booking_assignments(*)", payload)
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
  }.toString()

  fun friendlyMessage(error: Throwable): String {
    val message = error.message.orEmpty()
    return when {
      message.contains("401") || message.contains("session", true) -> "Your session expired. Please sign in again."
      message.contains("timeout", true) || message.contains("connect", true) || message.contains("network", true) ->
        "Please check your internet connection and try again."
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
    refreshIncomingRequests()
    addNotification(NotificationItem(title = "Shoot request declined", message = "You declined this request. We'll keep looking for other crew.", timestampText = "Just now", targetRole = Role.CREW, bookingId = bookingId))
  }

  fun declineShootRequest(bookingId: String) = crewDeclineBooking(bookingId, _currentUser.value.id)
  fun acceptShootRequest(bookingId: String, crewMember: AssignedCrewMember) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW) return
    applyCrewConfirmed(bookingId, crewMember)
  }

  private fun applyCrewConfirmed(bookingId: String, member: AssignedCrewMember) {
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
      SupabaseRestClient.post("rpc/accept_booking", JSONObject().apply { put("p_booking_id", bookingId) }.toString())
    }
  }

  fun cancelBooking(bookingId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId && it.status != BookingStatus.COMPLETED) it.copy(status = BookingStatus.CANCELLED) else it }
    if (_activeSearchingBookingId.value == bookingId) _activeSearchingBookingId.value = null
    refreshIncomingRequests()
    ioScope.launch { SupabaseRestClient.patch("bookings?id=eq.$bookingId", "{\"status\":\"CANCELLED\"}") }
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
  fun adminUpdateUserRole(newRole: Role) = switchRole(newRole)

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
        }.toString())
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
        }.toString())
      }
    }
    return true
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
            val localOnly = (_chatMessages.value[bookingId] ?: emptyList()).filter { local ->
              messages.none { remote -> remote.message == local.message && remote.isFromMe == local.isFromMe }
            }
            _chatMessages.value = _chatMessages.value + (bookingId to (messages + localOnly))
          }
        }
    }
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
          val merged = (_bookings.value + open).distinctBy { it.id }
          _bookings.value = merged
          refreshIncomingRequests()
        }
    }
    SupabaseRestClient.get("notifications?select=*&target_user_id=eq.${user.id}&order=created_at.desc")
      .onSuccess { raw -> _notifications.value = runCatching { parseNotifications(JSONArray(raw), user.role) }.getOrDefault(emptyList()) }
    SupabaseRestClient.get("favorite_crew?select=crew_id&client_id=eq.${user.id}")
      .onSuccess { raw ->
        _favoriteCrewIds.value = runCatching {
          val array = JSONArray(raw)
          buildSet { for (i in 0 until array.length()) add(array.getJSONObject(i).optString("crew_id")) }
        }.getOrDefault(emptySet())
      }
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
    SupabaseRestClient.get("crew_profiles?select=*&order=rating.desc")
      .onSuccess { raw -> _crewProfiles.value = runCatching { parseCrewProfiles(JSONArray(raw)) }.getOrDefault(emptyList()) }
  }

  /** UI uses friendly labels ("Today", "09:00 AM"); Supabase needs ISO date/time. */
  fun toSupabaseDate(display: String): String {
    val cal = java.util.Calendar.getInstance()
    when (display.trim().lowercase()) {
      "today" -> Unit
      "tomorrow" -> cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
      else -> {
        // Accept "Fri 12", "Sat 13" style chips: resolve day-of-month in current month.
        val day = display.filter { it.isDigit() }.toIntOrNull()
        if (day != null) {
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

  fun toSupabaseTime(display: String): String {
    // "09:00 AM" -> "09:00:00", "02:00 PM" -> "14:00:00". Fall back to raw HH:mm.
    val match = Regex("""(\d{1,2}):(\d{2})\s*([AaPp][Mm])?""").find(display.trim())
    if (match != null) {
      var hour = match.groupValues[1].toIntOrNull() ?: 9
      val minute = match.groupValues[2]
      val ampm = match.groupValues[3].uppercase()
      if (ampm == "PM" && hour < 12) hour += 12
      if (ampm == "AM" && hour == 12) hour = 0
      return "%02d:%s:00".format(hour, minute)
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

  private fun parseNotifications(array: JSONArray, role: Role): List<NotificationItem> = buildList {
    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      add(NotificationItem(o.optString("id"), o.optString("title"), o.optString("message"), o.optString("created_at"), role, o.optBoolean("is_read"), o.optString("booking_id").ifBlank { null }))
    }
  }

  private fun parseCrewProfiles(array: JSONArray): List<CrewProfile> = buildList {
    for (i in 0 until array.length()) {
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
