package com.example.data

import com.example.model.AssignedCrewMember
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.CrewProfile
import com.example.model.NotificationItem
import com.example.model.Role
import com.example.model.SavedLocation
import com.example.model.User
import com.example.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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

  fun setCurrentUser(user: User) {
    _currentUser.value = user
    _activeRole.value = user.role
    ioScope.launch { refreshFromSupabase(user) }
  }
  fun switchRole(role: Role) { _activeRole.value = role; _currentUser.value = _currentUser.value.copy(role = role) }
  fun setActiveSearchingBooking(bookingId: String?) { _activeSearchingBookingId.value = bookingId }

  fun toggleFavoriteCrew(crewId: String) {
    _favoriteCrewIds.value = _favoriteCrewIds.value.toMutableSet().apply { if (!add(crewId)) remove(crewId) }
  }

  fun toggleCrewAvailability(crewId: String? = null) {
    val targetId = crewId ?: _crewProfiles.value.firstOrNull { it.userId == _currentUser.value.id }?.id ?: return
    val next = !(_crewProfiles.value.firstOrNull { it.id == targetId }?.isAvailable ?: true)
    _isCrewAvailable.value = next
    _crewProfiles.value = _crewProfiles.value.map { if (it.id == targetId) it.copy(isAvailable = next) else it }
  }

  fun createBooking(newBooking: Booking): Booking {
    _bookings.value = listOf(newBooking) + _bookings.value
    _activeSearchingBookingId.value = newBooking.id
    refreshIncomingRequests()
    val user = _currentUser.value
    if (user.role == Role.CLIENT && user.id.isNotBlank()) {
      ioScope.launch {
        val payload = JSONObject().apply {
          put("id", newBooking.id)
          put("booking_code", newBooking.bookingCode)
          put("client_id", user.id)
          put("shoot_title", newBooking.shootTitle)
          put("category", newBooking.category.name)
          put("shoot_date", newBooking.dateText)
          put("shoot_time", newBooking.timeText)
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
        SupabaseRestClient.post("bookings", payload).onFailure {
          _bookings.value = _bookings.value.filterNot { it.id == newBooking.id }
          if (_activeSearchingBookingId.value == newBooking.id) _activeSearchingBookingId.value = null
          refreshIncomingRequests()
        }
      }
    }
    return newBooking
  }

  fun crewAcceptBooking(bookingId: String, crewId: String) {
    val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
    val crew = _crewProfiles.value.firstOrNull { it.id == crewId } ?: return
    if (booking.status != BookingStatus.SEARCHING_CREW || booking.assignedCrew.any { it.crewId == crewId }) return
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CONFIRMED, assignedCrew = it.assignedCrew + crew.toAssignedMember()) else it
    }
    _crewProfiles.value = _crewProfiles.value.map { if (it.id == crewId) it.copy(isAvailable = false) else it }
    _isCrewAvailable.value = false
    refreshIncomingRequests()
  }

  fun crewDeclineBooking(bookingId: String, crewId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    addNotification(NotificationItem("", "Shoot request declined", "A crew member is unavailable for this request.", "Just now", Role.ADMIN, false, bookingId))
  }

  fun declineShootRequest(bookingId: String) = crewDeclineBooking(bookingId, "")
  fun acceptShootRequest(bookingId: String, crewMember: AssignedCrewMember) = crewAcceptBooking(bookingId, crewMember.crewId)

  fun cancelBooking(bookingId: String) {
    if (_bookings.value.none { it.id == bookingId }) return
    _bookings.value = _bookings.value.map { if (it.id == bookingId && it.status != BookingStatus.COMPLETED) it.copy(status = BookingStatus.CANCELLED) else it }
    if (_activeSearchingBookingId.value == bookingId) _activeSearchingBookingId.value = null
    refreshIncomingRequests()
    ioScope.launch { SupabaseRestClient.patch("bookings?id=eq.$bookingId", "{\"status\":\"CANCELLED\"}") }
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
    _bookings.value = _bookings.value.map { if (it.id == bookingId) it.copy(status = newStatus) else it }
    refreshIncomingRequests()
  }
  fun updateBookingStatus(bookingId: String, status: BookingStatus) = adminUpdateBookingStatus(bookingId, status)
  fun adminVerifyCrew(crewId: String, status: VerificationStatus) { _crewProfiles.value = _crewProfiles.value.map { if (it.id == crewId) it.copy(verificationStatus = status) else it } }
  fun adminUpdateUserRole(newRole: Role) = switchRole(newRole)

  fun sendChatMessage(bookingId: String, text: String, senderRole: Role, senderName: String) {
    val message = text.trim()
    if (message.isEmpty() || _bookings.value.none { it.id == bookingId }) return
    val updated = (_chatMessages.value[bookingId] ?: emptyList()) + ChatMessage(bookingId = bookingId, senderName = senderName, senderRole = senderRole, message = message, timeText = "Just now", isFromMe = true)
    _chatMessages.value = _chatMessages.value + (bookingId to updated)
    val senderId = _currentUser.value.id
    if (senderId.isNotBlank()) {
      ioScope.launch {
        SupabaseRestClient.post("chat_messages", JSONObject().apply {
          put("booking_id", bookingId)
          put("sender_id", senderId)
          put("message", message)
        }.toString())
      }
    }
  }

  fun addNotification(item: NotificationItem) { _notifications.value = listOf(item) + _notifications.value }
  fun loadChatMessages(bookingId: String) {
    ioScope.launch {
      SupabaseRestClient.get("chat_messages?select=*&booking_id=eq.$bookingId&order=created_at.asc")
        .onSuccess { raw ->
          val userId = _currentUser.value.id
          val messages = runCatching {
            val array = JSONArray(raw)
            buildList {
              for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val senderRole = if (o.optString("sender_id") == userId) _currentUser.value.role else Role.CREW
                add(ChatMessage(o.optString("id"), bookingId, "", senderRole, o.optString("message"), o.optString("created_at"), o.optString("sender_id") == userId))
              }
            }
          }.getOrDefault(emptyList())
          _chatMessages.value = _chatMessages.value + (bookingId to messages)
        }
    }
  }
  private fun refreshIncomingRequests() { _incomingShootRequests.value = _bookings.value.filter { it.status == BookingStatus.SEARCHING_CREW } }
  fun markAllNotificationsRead(role: Role? = null) { _notifications.value = _notifications.value.map { if (role == null || it.targetRole == role) it.copy(isRead = true) else it } }

  private fun CrewProfile.toAssignedMember() = AssignedCrewMember(id, fullName, primaryRole, phone, gearSummary, rating, verificationStatus == VerificationStatus.VERIFIED)

  private suspend fun refreshFromSupabase(user: User) {
    if (user.id.isBlank()) return
    SupabaseRestClient.get("bookings?select=*&client_id=eq.${user.id}&order=created_at.desc")
      .onSuccess { raw ->
        val loaded = runCatching { parseBookings(JSONArray(raw)) }.getOrDefault(emptyList())
        _bookings.value = loaded
        refreshIncomingRequests()
      }
    SupabaseRestClient.get("notifications?select=*&target_user_id=eq.${user.id}&order=created_at.desc")
      .onSuccess { raw -> _notifications.value = runCatching { parseNotifications(JSONArray(raw), user.role) }.getOrDefault(emptyList()) }
    SupabaseRestClient.get("crew_profiles?select=*&order=rating.desc")
      .onSuccess { raw -> _crewProfiles.value = runCatching { parseCrewProfiles(JSONArray(raw)) }.getOrDefault(emptyList()) }
  }

  private fun parseBookings(array: JSONArray): List<Booking> = buildList {
    for (i in 0 until array.length()) {
      val o = array.getJSONObject(i)
      val requirements = o.optJSONArray("crew_requirements") ?: JSONArray()
      val parsedRequirements = buildList {
        for (j in 0 until requirements.length()) {
          val r = requirements.getJSONObject(j)
          val role = runCatching { com.example.model.CrewRoleType.valueOf(r.optString("role")) }.getOrNull() ?: continue
          add(com.example.model.CrewRequirement(role, r.optInt("quantity", 1)))
        }
      }
      val category = runCatching { com.example.model.ShootCategory.valueOf(o.optString("category")) }.getOrDefault(com.example.model.ShootCategory.VIDEO)
      val status = runCatching { BookingStatus.valueOf(o.optString("status")) }.getOrDefault(BookingStatus.SEARCHING_CREW)
      add(Booking(
        id = o.optString("id"), bookingCode = o.optString("booking_code"), shootTitle = o.optString("shoot_title"),
        category = category, dateText = o.optString("shoot_date"), timeText = o.optString("shoot_time"),
        durationHours = o.optInt("duration_hours", 1), venueName = o.optString("venue_name"), fullAddress = o.optString("full_address"),
        locationInstructions = o.optString("location_instructions"), crewRequirements = parsedRequirements,
        shootDescription = o.optString("shoot_description"), specialInstructions = o.optString("special_instructions"),
        brandName = o.optString("brand_name"), referenceLink = o.optString("reference_link"), status = status
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
      add(CrewProfile(o.optString("id"), o.optString("user_id"), "", "", "", o.optString("city"), role, emptyList(), o.optInt("experience_years"), o.optString("bio"), o.optString("gear_summary"), o.optString("portfolio_url"), o.optString("instagram_handle"), verification, o.optBoolean("is_available"), o.optDouble("rating"), o.optInt("total_shoots_completed")))
    }
  }
}
