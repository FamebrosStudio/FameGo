package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class SupabaseAuthResult(
  val id: String,
  val email: String,
  val accessToken: String,
  val refreshToken: String
)

/** Small REST auth adapter kept independent from the app's UI and local state. */
object SupabaseAuthClient {
  private val http = OkHttpClient()
  private val jsonType = "application/json".toMediaType()

  suspend fun authenticate(
    email: String,
    password: String,
    signUp: Boolean,
    name: String,
    phone: String,
    role: String,
    companyName: String
  ): Result<SupabaseAuthResult> = withContext(Dispatchers.IO) {
    if (!SupabaseConfig.isConfigured) return@withContext Result.failure(IllegalStateException("Supabase is not configured"))
    val endpoint = if (signUp) {
      "${SupabaseConfig.baseUrl}/auth/v1/signup"
    } else {
      "${SupabaseConfig.baseUrl}/auth/v1/token?grant_type=password"
    }
    val body = JSONObject().apply {
      put("email", email)
      put("password", password)
      if (signUp) {
        put("data", JSONObject().apply {
          put("full_name", name)
          put("phone", phone)
          put("role", role)
          put("company_name", companyName)
        })
      }
    }.toString().toRequestBody(jsonType)
    val request = Request.Builder()
      .url(endpoint)
      .header("apikey", SupabaseConfig.publishableKey)
      .header("Authorization", "Bearer ${SupabaseConfig.publishableKey}")
      .header("Content-Type", "application/json")
      .post(body)
      .build()
    runCatching {
      http.newCall(request).execute().use { response ->
        val raw = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          val message = runCatching { JSONObject(raw).optString("msg").ifBlank { JSONObject(raw).optString("message") } }.getOrNull()
          error(message?.ifBlank { "Authentication failed (${response.code})" } ?: "Authentication failed (${response.code})")
        }
        val root = JSONObject(raw)
        val user = root.optJSONObject("user") ?: root
        val accessToken = root.optString("access_token")
        if (accessToken.isBlank()) {
          // Email-confirmation flow: Supabase created the user but issued no session.
          val confirmation = root.optString("message").ifBlank { raw.take(200) }
          error("Account created — please confirm your email, then sign in. $confirmation")
        }
        SupabaseAuthResult(
          id = user.optString("id").ifBlank { error("Supabase did not return a user id") },
          email = user.optString("email", email),
          accessToken = accessToken,
          refreshToken = root.optString("refresh_token")
        ).also { SupabaseSession.save(it.accessToken, it.refreshToken) }
      }
    }
  }

  suspend fun restoreSession(): Result<SupabaseAuthResult?> = withContext(Dispatchers.IO) {
    val refresh = SupabaseSession.refreshToken
    if (!SupabaseConfig.isConfigured || refresh.isNullOrBlank()) return@withContext Result.success(null)
    val body = JSONObject().put("refresh_token", refresh).toString().toRequestBody(jsonType)
    val request = Request.Builder()
      .url("${SupabaseConfig.baseUrl}/auth/v1/token?grant_type=refresh_token")
      .header("apikey", SupabaseConfig.publishableKey)
      .header("Content-Type", "application/json")
      .post(body).build()
    runCatching {
      http.newCall(request).execute().use { response ->
        val raw = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          SupabaseSession.clear()
          return@use null
        }
        val root = JSONObject(raw)
        val user = root.getJSONObject("user")
        SupabaseAuthResult(
          id = user.getString("id"),
          email = user.optString("email"),
          accessToken = root.getString("access_token"),
          refreshToken = root.getString("refresh_token")
        ).also { SupabaseSession.save(it.accessToken, it.refreshToken) }
      }
    }
  }

  fun friendlyMessage(error: Throwable, signingUp: Boolean): String {
    val value = error.message.orEmpty().lowercase()
    return when {
      "already registered" in value || "already exists" in value -> "An account already exists for this email."
      "invalid login" in value || "invalid credentials" in value -> "Email or password is incorrect."
      "email not confirmed" in value -> "Confirm your email before signing in."
      "timeout" in value || "connect" in value || "network" in value -> "Please check your internet connection."
      signingUp -> "Unable to create account. Please try again."
      else -> "Unable to sign in. Please try again."
    }
  }
}
