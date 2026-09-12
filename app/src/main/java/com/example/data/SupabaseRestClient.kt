package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object SupabaseRestClient {
  private val client = OkHttpClient()
  private val json = "application/json".toMediaType()

  suspend fun get(path: String): Result<String> = call("GET", path)
  suspend fun post(path: String, body: String): Result<String> = call("POST", path, body)
  suspend fun patch(path: String, body: String): Result<String> = call("PATCH", path, body)

  private suspend fun call(method: String, path: String, body: String? = null): Result<String> = withContext(Dispatchers.IO) {
    if (!SupabaseConfig.isConfigured) return@withContext Result.failure(IllegalStateException("Supabase is not configured"))
    val builder = Request.Builder().url("${SupabaseConfig.baseUrl}/rest/v1/$path")
      .header("apikey", SupabaseConfig.publishableKey)
      .header("Authorization", "Bearer ${SupabaseSession.accessToken ?: SupabaseConfig.publishableKey}")
      .header("Accept", "application/json")
    if (body != null) builder.header("Content-Type", "application/json")
    val request = when (method) {
      "POST" -> builder.post(body.orEmpty().toRequestBody(json)).build()
      "PATCH" -> builder.patch(body.orEmpty().toRequestBody(json)).build()
      else -> builder.get().build()
    }
    runCatching {
      client.newCall(request).execute().use { response ->
        val raw = response.body?.string().orEmpty()
        if (!response.isSuccessful) error("Supabase ${response.code}: ${raw.take(240)}")
        raw
      }
    }
  }
}
