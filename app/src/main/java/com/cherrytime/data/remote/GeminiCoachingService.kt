package com.cherrytime.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiCoachingService @Inject constructor() {

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"

    suspend fun getTip(apiKey: String, prompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL("$endpoint?key=$apiKey")
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    doOutput = true
                }
                val body = buildPromptJson(prompt)
                connection.outputStream.use { it.write(body.toByteArray()) }

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    parseText(response) ?: error("Empty response from Gemini")
                } else {
                    error("Gemini HTTP ${connection.responseCode}")
                }
            }
        }

    suspend fun validateKey(apiKey: String): Boolean =
        getTip(apiKey, "Reply with just the word 'ok'.").isSuccess

    private fun buildPromptJson(prompt: String): String {
        val safePrompt = prompt.replace("\"", "'")
        return """{"contents":[{"parts":[{"text":"$safePrompt"}]}]}"""
    }

    private fun parseText(json: String): String? = runCatching {
        JSONObject(json)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
    }.getOrNull()
}
