package com.example.data

import android.os.Handler
import android.os.Looper
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Multiplexed channel adapter: ONE websocket carries every subscription
 * (chat, notifications, bookings) instead of one socket per channel.
 * Less radio wakeups, less battery, less data — and a single reconnect path.
 * The server remains the source of truth; a change only triggers a refresh.
 */
object SupabaseRealtimeClient {
  private val http = SupabaseNetwork.http
  private data class Channel(val topic: String, val changes: org.json.JSONArray, val onChanged: () -> Unit)
  private val channels = ConcurrentHashMap<String, Channel>()
  private val lock = Any()
  @Volatile private var socket: WebSocket? = null
  private val refCounter = AtomicInteger(1)
  private val mainHandler = Handler(Looper.getMainLooper())
  private val reconnectRunnable = Runnable {
    synchronized(lock) {
      if (socket == null && channels.isNotEmpty()) connectLocked()
    }
  }

  fun subscribeToChat(bookingId: String, onChanged: () -> Unit) {
    if (!SupabaseConfig.isConfigured || SupabaseSession.accessToken.isNullOrBlank() || bookingId.isBlank()) return
    addChannel(
      key = "chat:$bookingId",
      topic = "realtime:booking_$bookingId",
      changes = org.json.JSONArray().put(JSONObject().apply {
        put("event", "*")
        put("schema", "public")
        put("table", "chat_messages")
        put("filter", "booking_id=eq.$bookingId")
      }),
      onChanged = onChanged
    )
  }

  /** Bell + lists stay live: notifications addressed to this user. */
  fun subscribeToNotifications(userId: String, onChanged: () -> Unit) {
    if (!SupabaseConfig.isConfigured || SupabaseSession.accessToken.isNullOrBlank() || userId.isBlank()) return
    addChannel(
      key = "notifications:$userId",
      topic = "realtime:notifications_$userId",
      changes = org.json.JSONArray().put(JSONObject().apply {
        put("event", "*")
        put("schema", "public")
        put("table", "notifications")
        put("filter", "target_user_id=eq.$userId")
      }),
      onChanged = onChanged
    )
  }

  /** Crew discover new paid requests and clients see status flips without refresh. */
  fun subscribeToBookings(key: String, onChanged: () -> Unit) {
    if (!SupabaseConfig.isConfigured || SupabaseSession.accessToken.isNullOrBlank()) return
    addChannel(
      key = "bookings:$key",
      topic = "realtime:bookings_$key",
      changes = org.json.JSONArray().put(JSONObject().apply {
        put("event", "*")
        put("schema", "public")
        put("table", "bookings")
      }),
      onChanged = onChanged
    )
  }

  fun unsubscribeFromChat(bookingId: String) {
    removeChannel("chat:$bookingId")
  }

  private fun addChannel(key: String, topic: String, changes: org.json.JSONArray, onChanged: () -> Unit) {
    channels[key] = Channel(topic, changes, onChanged)
    synchronized(lock) {
      val ws = socket
      if (ws == null) connectLocked()
      else sendJoin(ws, topic, changes)
    }
  }

  private fun removeChannel(key: String) {
    channels.remove(key)
    synchronized(lock) {
      if (channels.isEmpty()) {
        socket?.close(1000, "idle")
        socket = null
      }
    }
  }

  private fun connectLocked() {
    val socketUrl = SupabaseConfig.baseUrl.replaceFirst("https://", "wss://") +
      "/realtime/v1/websocket?apikey=${SupabaseConfig.publishableKey}&vsn=1.0.0"
    val request = Request.Builder().url(socketUrl).build()
    socket = http.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        // (Re)join every channel on the single socket.
        channels.values.forEach { sendJoin(webSocket, it.topic, it.changes) }
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        runCatching {
          val root = JSONObject(text)
          if (root.optString("event") != "postgres_changes") return
          val topic = root.optString("topic")
          val targets = if (topic.isBlank()) channels.values
          else channels.values.filter { it.topic == topic }.ifEmpty { channels.values }
          targets.forEach { runCatching { it.onChanged() } }
        }
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        synchronized(lock) {
          if (socket === webSocket) socket = null
        }
        // One delayed reconnect for all channels (network blips, sleep/wake).
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.postDelayed(reconnectRunnable, 3000)
      }
    })
  }

  private fun sendJoin(webSocket: WebSocket, topic: String, changes: org.json.JSONArray) {
    val join = JSONObject().apply {
      put("event", "phx_join")
      put("topic", topic)
      put("ref", refCounter.getAndIncrement().toString())
      put("payload", JSONObject().apply {
        put("config", JSONObject().apply {
          put("private", false)
          put("postgres_changes", changes)
        })
        put("access_token", SupabaseSession.accessToken)
      })
    }
    runCatching { webSocket.send(join.toString()) }
  }

  /** Call on logout so no socket keeps firing callbacks for the old session. */
  fun closeAll() {
    mainHandler.removeCallbacks(reconnectRunnable)
    synchronized(lock) {
      socket?.close(1000, "logout")
      socket = null
    }
    channels.clear()
  }
}
