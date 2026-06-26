package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiTranslateHelper {
    private const val TAG = "GeminiTranslateHelper"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Translates the given text.
     * If the input is primarily Bengali, it translates to English.
     * If primarily English, it translates to Bengali.
     */
    suspend fun translateText(text: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please add it in the Secrets panel in AI Studio."))
        }

        if (text.isBlank()) {
            return@withContext Result.failure(Exception("Text is empty"))
        }

        // Auto detect language
        val hasBengali = text.contains(Regex("[\\u0980-\\u09FF]"))
        val targetLang = if (hasBengali) "English" else "Bengali (বাংলা)"
        val sourceLang = if (hasBengali) "Bengali (বাংলা)" else "English"

        val systemInstruction = """
            You are an expert professional translator specializing in English and Bengali (বাংলা) translations.
            Your task is to translate the input text from $sourceLang to $targetLang accurately, preserving the original tone, context, and nuances.
            Return ONLY the translated text. Do not include any explanations, definitions, introductory greetings, or metadata.
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", text)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API request failed with code ${response.code}: ${response.message}"))
                }

                val responseBodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                val responseJson = JSONObject(responseBodyStr)

                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                val translatedText = firstPart?.optString("text")

                if (!translatedText.isNullOrBlank()) {
                    Result.success(translatedText.trim())
                } else {
                    Result.failure(Exception("No translation text found in response"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Smartly enhances or corrects spelling/grammar for the input text (Bangla or English).
     */
    suspend fun improveText(text: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured."))
        }

        if (text.isBlank()) {
            return@withContext Result.failure(Exception("Text is empty"))
        }

        val hasBengali = text.contains(Regex("[\\u0980-\\u09FF]"))
        val language = if (hasBengali) "Bengali" else "English"

        val instruction = if (hasBengali) {
            "নিচের বাংলা লেখার বানান ও ব্যাকরণগত ভুল সংশোধন করো এবং মানসম্মত ভাষায় রূপান্তর করো। কোনো অতিরিক্ত কথা না বলে শুধু সংশোধিত লেখাটি ফিরিয়ে দাও:"
        } else {
            "Correct spelling, grammar, and polish the style of the following English text. Return ONLY the polished text with no extra explanations:"
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$instruction\n\n$text")
                            })
                        })
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API request failed with code ${response.code}"))
                }

                val responseBodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                val responseJson = JSONObject(responseBodyStr)

                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                val improvedText = firstPart?.optString("text")

                if (!improvedText.isNullOrBlank()) {
                    Result.success(improvedText.trim())
                } else {
                    Result.failure(Exception("No response text found"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Improve text failed", e)
            Result.failure(e)
        }
    }
}
